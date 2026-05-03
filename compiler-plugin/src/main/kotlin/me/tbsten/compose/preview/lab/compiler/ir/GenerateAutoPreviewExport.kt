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
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Emits the per-module auto-provider function whose `List<CollectedPreview>` return value is
 * the canonical entry point downstream `collectAllModulePreviews()` calls invoke for this
 * module.
 *
 * **Output (semantically equivalent Kotlin)** for a module whose Kotlin module name hashes
 * to `a3k9z2x1`:
 *
 * ```kotlin
 * // me/tbsten/compose/preview/lab/exports/PreviewLabAutoProvider_a3k9z2x1.kt
 * package me.tbsten.compose.preview.lab.exports
 *
 * public fun previewLabAutoProvider_a3k9z2x1(): List<CollectedPreview> = listOf(
 *     CollectedPreview(id = "com.example.MyPreview", ...) { MyPreview() },
 *     // ...
 * )
 * ```
 *
 * The function name is computed by [computeAutoProviderName] from the Kotlin module name
 * hash so two modules sharing the same source-level package never collide on the same FQN.
 *
 * **Discovery path (Kotlin 2.3.21+, all platforms)**: `PreviewLabHintFirGenerator` emits a
 * per-module `previewLabExport(value: <Marker>): Unit` hint whose marker class id ends with
 * the same hash this generator embeds in its provider function name. Downstream
 * [PreviewListIrBuilder.collectDependencyGetters] reads the hint's parameter type, extracts
 * the hash, and reconstructs this provider's FQN — no `@PreviewExportHint` annotation lookup
 * is involved (and `PreviewLabHintIrBodyFiller` only injects an empty body, never an
 * annotation).
 *
 * The provider function is registered with [IrPluginContext.metadataDeclarationRegistrar] so
 * downstream `referenceFunctions(...)` finds it.
 */
internal class GenerateAutoPreviewExport(
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
     * Emits the auto-provider function (and, on legacy paths, the matching legacy hint) into
     * [moduleFragment].
     *
     * **Input**: Module containing `@Preview` functions, with [sourceFile] being one of its
     * real source files (its `FirMetadataSource.File` seeds the synthetic file metadata so the
     * provider lands in `kotlin.Metadata(k=2)` and is discoverable via `referenceFunctions`).
     *
     * **Output**: Synthetic provider function as a new IrFile in
     * `me.tbsten.compose.preview.lab.exports`. The matching hint function is emitted by
     * [me.tbsten.compose.preview.lab.compiler.fir.PreviewLabHintFirGenerator] during the FIR
     * pass, so this generator does not emit any hint itself.
     *
     * Defined as `operator fun invoke` so call sites read like `generator(sourceFile)` — the
     * class name `GenerateAutoPreviewExport` already carries the verb.
     */
    operator fun invoke(sourceFile: IrFile) {
        if (previews.isEmpty()) return

        val providerFunctionName = computeAutoProviderName(moduleFragment)
        val moduleHash = providerFunctionName.asString().removePrefix(AutoProviderPrefix)

        val syntheticFile = createSyntheticFile(moduleHash, sourceFile)
        val providerFunction = buildProviderFunction(providerFunctionName, syntheticFile)

        moduleFragment.addFile(
            syntheticFile.apply { addChild(providerFunction) },
        )

        pluginContext.metadataDeclarationRegistrar.registerFunctionAsMetadataVisible(providerFunction)

        // The matching `previewLabExport(<Marker>)` hint is emitted by
        // [PreviewLabHintFirGenerator] in the FIR pass; no IR-side hint emission needed.
    }

    /**
     * Builds the provider function whose body returns the list of all collected previews.
     *
     * **Generated function** (semantically equivalent Kotlin):
     * ```kotlin
     * public fun previewLabAutoProvider_<moduleHash>(): List<CollectedPreview> {
     *     return listOf(
     *         CollectedPreview(id = "id1", ...) { Preview1() },
     *         CollectedPreview(id = "id2", ...) { Preview2() },
     *     )
     * }
     * ```
     *
     * The list contains every `@Preview` function discovered in the module. Unlike the manual
     * `val x by collectModulePreviews()` path, the result is *not* wrapped in `lazy { ... }` or
     * `PreviewExport(...)` — the function returns `List<CollectedPreview>` directly so
     * [PreviewListIrBuilder.collectDependencyGetters] can call it as a plain getter.
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
    private fun createSyntheticFile(uniqueSuffix: String, sourceFile: IrFile): IrFileImpl {
        val fileName = "PreviewLabAutoProvider_$uniqueSuffix.kt"

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
}
