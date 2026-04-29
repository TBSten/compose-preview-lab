@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.IrDeclarationOriginCompat
import me.tbsten.compose.preview.lab.compiler.compat.addConstructorCallAnnotationWithArgs
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.impl.EmptyPackageFragmentDescriptor
import org.jetbrains.kotlin.fir.backend.FirMetadataSource
import org.jetbrains.kotlin.fir.builder.buildPackageDirective
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.builder.buildFile
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.addFile
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Generates synthetic hint functions in the [GeneratePreviewExportHint.HINT_PACKAGE] package
 * so that downstream `collectAllModulePreviews()` calls can discover preview properties from
 * dependency modules without any Gradle configuration.
 *
 * For each `val x by collectModulePreviews()` declaration, this generator emits one IR file
 * containing a function shaped like:
 *
 * ```
 * @PreviewExportHint(fqn = "uiLib.uiLibPreviews")
 * fun previewLabExport(value: PreviewExport): Unit {}
 * ```
 *
 * **JVM-only by design.** All hints share the same `(previewLabExport, PreviewExport)` IR
 * signature. On JVM the file-facade class disambiguates them at the bytecode level so
 * `referenceFunctions(...)` returns every overload across the classpath. KLIB-based platforms
 * (JS / Wasm / iOS) compute IdSignature from `(name, parameter types)` only and would treat
 * these as duplicates at link time. Callers (currently `PreviewLabIrBodyFiller`) must therefore
 * gate this generator on `pluginContext.platform.isJvm()`; cross-module aggregation is JVM-only
 * for now.
 *
 * The hint function is registered with [IrPluginContext.metadataDeclarationRegistrar] so that
 * `pluginContext.referenceFunctions(...)` in downstream compilations can find every overload
 * across the classpath. The annotation argument carries the original property's fully-qualified
 * name, which the consumer uses to resolve the property via [IrPluginContext.referenceProperties].
 *
 * Adapted from Metro's `HintGenerator` pattern.
 */
internal class GeneratePreviewExportHint(
    private val pluginContext: IrPluginContext,
    private val moduleFragment: IrModuleFragment,
    private val compatContext: CompatContext,
) {
    private val previewExportClass by lazy {
        pluginContext.referenceClass(PREVIEW_EXPORT_CLASS_ID)
            ?: error("PreviewExport class not found on classpath")
    }

    private val previewExportType by lazy { previewExportClass.typeWith() }

    private val hintAnnotationClass by lazy {
        pluginContext.referenceClass(PREVIEW_EXPORT_HINT_CLASS_ID)
            ?: error("PreviewExportHint annotation class not found on classpath")
    }

    /**
     * Generates one hint function for [propertyFqn] and adds it to a synthetic file.
     * [sourceFile] supplies the [FirMetadataSource.File] required to make the synthetic file
     * a proper Kotlin file facade (k=2 in `kotlin.Metadata`) so that downstream
     * `pluginContext.referenceFunctions(...)` can discover its callables.
     *
     * Defined as `operator fun invoke` so call sites read like `hintGenerator(fqn, file)`
     * — the class name `GeneratePreviewExportHint` already carries the verb.
     */
    operator fun invoke(propertyFqn: String, sourceFile: IrFile) {
        val hintFunction = pluginContext.irFactory.buildFun {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = HINT_FUNCTION_NAME
            origin = IrDeclarationOriginCompat.DEFINED
            returnType = pluginContext.irBuiltIns.unitType
            visibility = DescriptorVisibilities.PUBLIC
        }.apply {
            parameters += buildValueParameter(this) {
                name = HINT_VALUE_PARAM_NAME
                type = previewExportType
                kind = IrParameterKind.Regular
            }
            body = buildStubBody(this)
            attachHintAnnotation(this, propertyFqn)
        }

        moduleFragment.addFile(

            createHintFile(propertyFqn, sourceFile).apply {
                addChild(hintFunction)
            },
        )

        pluginContext.metadataDeclarationRegistrar.registerFunctionAsMetadataVisible(hintFunction)
    }

    /**
     * The hint function is a metadata marker — it is never actually invoked. An empty
     * `Unit`-returning body avoids cross-version codegen quirks (e.g. K2.4 wrapping
     * `Nothing`-typed expressions in synthesized `throwKotlinNothingValueException` calls
     * which then fail in JvmIrCodegen).
     */
    private fun buildStubBody(function: IrSimpleFunction): IrBlockBody {
        val builder = DeclarationIrBuilder(pluginContext, function.symbol)
        return builder.irBlockBody { }
    }

    private fun attachHintAnnotation(function: IrSimpleFunction, propertyFqn: String) {
        val constructor = hintAnnotationClass.constructors.first()
        val fqnArg = IrConstImpl.string(
            SYNTHETIC_OFFSET,
            SYNTHETIC_OFFSET,
            pluginContext.irBuiltIns.stringType,
            propertyFqn,
        )
        function.addConstructorCallAnnotationWithArgs(
            type = hintAnnotationClass.typeWith(),
            constructorSymbol = constructor,
            arguments = listOf(fqnArg),
        )
    }

    private fun createHintFile(propertyFqn: String, sourceFile: IrFile): IrFileImpl {
        val sanitizedFqn = propertyFqn.replace('.', '_')
        val fileName = "PreviewLabExport_$sanitizedFqn.kt"

        // Source file's metadata gives us the FirModuleData we need to wire up a proper
        // FirFile-backed metadata source for the synthetic hint file. Without this, the
        // synthetic class is emitted with `kotlin.Metadata(k=3)` (synthetic class) and
        // downstream `pluginContext.referenceFunctions(...)` cannot discover the hint
        // function as a top-level callable.
        val sourceMetadata = sourceFile.metadata as? FirMetadataSource.File
            ?: error(
                "ComposePreviewLab: source file ${sourceFile.fileEntry.name} has no FirMetadataSource.File " +
                    "(found ${sourceFile.metadata?.javaClass?.canonicalName}). Hint generation requires K2 FIR.",
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
        val HINT_PACKAGE: FqName = FqName("me.tbsten.compose.preview.lab.exports")
        val HINT_FUNCTION_NAME: Name = Name.identifier("previewLabExport")
        val HINT_VALUE_PARAM_NAME: Name = Name.identifier("value")
        val HINT_FUNCTION_CALLABLE_ID: CallableId = CallableId(HINT_PACKAGE, HINT_FUNCTION_NAME)

        private val PREVIEW_EXPORT_CLASS_ID = ClassId(
            FqName("me.tbsten.compose.preview.lab"),
            Name.identifier("PreviewExport"),
        )

        val PREVIEW_EXPORT_FQN: FqName = PREVIEW_EXPORT_CLASS_ID.asSingleFqName()

        private val PREVIEW_EXPORT_HINT_CLASS_ID = ClassId(
            FqName("me.tbsten.compose.preview.lab"),
            Name.identifier("PreviewExportHint"),
        )

        val PREVIEW_EXPORT_HINT_FQN: FqName = PREVIEW_EXPORT_HINT_CLASS_ID.asSingleFqName()
    }
}
