@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.impl.EmptyPackageFragmentDescriptor
import org.jetbrains.kotlin.fir.backend.FirMetadataSource
import org.jetbrains.kotlin.fir.builder.buildPackageDirective
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.builder.buildFile
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.addFile
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.isJvm

/**
 * Emits an auto-generated preview export — a stand-alone provider function plus a matching
 * [GeneratePreviewExportHint] entry — for modules that have `@Preview` functions but never
 * declare a `collectModulePreviews()` / `collectAllModulePreviews()` sentinel.
 *
 * This is the half of cross-module discovery that lets users skip the boilerplate
 * `val xxxPreviews by collectModulePreviews()` declaration entirely: as long as the Gradle
 * plugin is applied and at least one `@Preview` function exists, downstream
 * `collectAllModulePreviews()` can pick up that module's previews.
 *
 * **Output (semantically equivalent Kotlin)** for a module whose first `@Preview` function
 * lives in `package com.example.lib`:
 *
 * ```kotlin
 * // me/tbsten/compose/preview/lab/exports/PreviewLabAutoProvider_com_example_lib.kt
 * package me.tbsten.compose.preview.lab.exports
 *
 * public fun previewLabAutoProvider_com_example_lib(): List<CollectedPreview> = listOf(
 *     CollectedPreview(id = "com.example.lib.MyPreview", ...) { MyPreview() },
 *     // ...
 * )
 *
 * // (separate synthetic file emitted by GeneratePreviewExportHint)
 * @PreviewExportHint(fqn = "me.tbsten.compose.preview.lab.exports.previewLabAutoProvider_com_example_lib")
 * public fun previewLabExport(value: PreviewExport): Unit {}
 * ```
 *
 * The provider function is registered with [IrPluginContext.metadataDeclarationRegistrar] so
 * downstream `referenceFunctions(...)` can find it via the fallback path in
 * [PreviewListIrBuilder] (`referenceProperties` first, then `referenceFunctions`).
 *
 * **JVM-only by design** — same constraint as [GeneratePreviewExportHint]; see its KDoc for
 * why KLIB-based platforms can't host the hint shape.
 */
internal class AutoPreviewExportGenerator(
    private val pluginContext: IrPluginContext,
    private val moduleFragment: IrModuleFragment,
    private val compatContext: CompatContext,
    private val previews: List<PreviewFunctionInfo>,
    private val config: PluginConfig,
) {
    private val collectedPreviewType by lazy {
        pluginContext.referenceClass(
            ClassId(FqName("me.tbsten.compose.preview.lab"), Name.identifier("CollectedPreview")),
        )!!.typeWith()
    }

    private val listOfCollectedPreviewType by lazy {
        pluginContext.referenceClass(
            ClassId(FqName("kotlin.collections"), Name.identifier("List")),
        )!!.typeWith(collectedPreviewType)
    }

    /**
     * Emits the auto provider + hint pair into [moduleFragment].
     *
     * [sourceFile] is required for the synthetic file's `FirMetadataSource.File` wiring so the
     * provider function ends up in `kotlin.Metadata(k=2)` (file facade) and is visible to
     * downstream `referenceFunctions(...)` lookups. Pass any source file that already carries a
     * `FirMetadataSource.File` — typically the file holding the first `@Preview` function.
     */
    fun generate(sourceFile: IrFile) {
        if (previews.isEmpty()) return
        if (pluginContext.platform?.isJvm() != true) return

        val firstPreviewFile = previews.first().function.file
        val firstPreviewPkg = firstPreviewFile.packageFqName.asString()
        val sanitizedPkg = firstPreviewPkg.replace('.', '_').ifEmpty { "default" }
        val providerFunctionName = Name.identifier("previewLabAutoProvider_$sanitizedPkg")
        val providerFqn = "${HINT_PACKAGE.asString()}.${providerFunctionName.asString()}"

        val syntheticFile = createSyntheticFile(sanitizedPkg, sourceFile)
        val providerFunction = buildProviderFunction(providerFunctionName, syntheticFile)

        moduleFragment.addFile(
            syntheticFile.apply { addChild(providerFunction) },
        )

        pluginContext.metadataDeclarationRegistrar.registerFunctionAsMetadataVisible(providerFunction)

        // Emit the matching hint that points to the provider function's FQN. The hint lives in
        // its own synthetic file (separate file facade) — same pattern as the manual case where
        // each `collectModulePreviews()` property gets its own hint file.
        GeneratePreviewExportHint(pluginContext, moduleFragment, compatContext)
            .invoke(providerFqn, sourceFile)
    }

    /**
     * Builds the provider function whose body returns the list of all collected previews.
     *
     * **Generated function** (semantically equivalent Kotlin):
     * ```kotlin
     * public fun previewLabAutoProvider_<sanitizedPkg>(): List<CollectedPreview> {
     *     return listOf(
     *         CollectedPreview(id = "id1", ...) { Preview1() },
     *         CollectedPreview(id = "id2", ...) { Preview2() },
     *     )
     * }
     * ```
     *
     * Unlike the manual `val x by collectModulePreviews()` path, the result is *not* wrapped in
     * `lazy { ... }` or `PreviewExport(...)` — the function returns `List<CollectedPreview>`
     * directly so [PreviewListIrBuilder.collectDependencyGetters] can call it as a plain getter.
     */
    private fun buildProviderFunction(providerFunctionName: Name, parent: IrFile,): IrSimpleFunction {
        val providerFunction = pluginContext.irFactory.buildFun {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = providerFunctionName
            origin = IrDeclarationOriginCompat.DEFINED
            returnType = listOfCollectedPreviewType
            visibility = DescriptorVisibilities.PUBLIC
        }
        providerFunction.parent = parent

        val previewListBuilder = PreviewListIrBuilder(pluginContext, previews, config, compatContext)
        val builder = DeclarationIrBuilder(pluginContext, providerFunction.symbol)
        val listExpr = previewListBuilder.buildPreviewsListExpr(builder, providerFunction)
        providerFunction.body = builder.irBlockBody {
            +irReturn(listExpr)
        }

        return providerFunction
    }

    /**
     * Creates the synthetic [IrFile] in [HINT_PACKAGE] that hosts the provider function.
     *
     * Mirrors `GeneratePreviewExportHint.createHintFile` — the `FirMetadataSource.File` wiring
     * is what causes the synthetic file to be emitted as a Kotlin file facade
     * (`kotlin.Metadata(k=2)`), which is the prerequisite for `referenceFunctions(...)` to
     * discover its top-level callables in downstream compilations.
     */
    private fun createSyntheticFile(sanitizedPkg: String, sourceFile: IrFile): IrFileImpl {
        val fileName = "PreviewLabAutoProvider_$sanitizedPkg.kt"

        val sourceMetadata = sourceFile.metadata as? FirMetadataSource.File
            ?: error(
                "ComposePreviewLab: source file ${sourceFile.fileEntry.name} has no FirMetadataSource.File " +
                    "(found ${sourceFile.metadata?.javaClass?.canonicalName}). Auto provider generation requires K2 FIR.",
            )
        val firFile = buildFile {
            moduleData = sourceMetadata.fir.moduleData
            origin = FirDeclarationOrigin.Synthetic.PluginFile
            packageDirective = buildPackageDirective { packageFqName = HINT_PACKAGE }
            name = fileName
        }

        return IrFileImpl(
            fileEntry = compatContext.createSyntheticFileEntry(fileName),
            packageFragmentDescriptor = EmptyPackageFragmentDescriptor(
                moduleFragment.descriptor,
                HINT_PACKAGE,
            ),
            module = moduleFragment,
        ).also { it.metadata = FirMetadataSource.File(firFile) }
    }

    companion object {
        // Reuse the same package as hint functions so downstream
        // `collectDependencyGetters()` can resolve provider functions through its existing
        // `referenceFunctions(CallableId(...))` fallback path.
        val HINT_PACKAGE: FqName = GeneratePreviewExportHint.HINT_PACKAGE
    }
}
