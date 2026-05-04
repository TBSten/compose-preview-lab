@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.getAnnotationCompat
import me.tbsten.compose.preview.lab.compiler.compat.hasAnnotationCompat
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.platform.jvm.isJvm

/**
 * IR generation extension for Compose Preview Lab.
 *
 * Collects every @Preview function in the module and injects them at the
 * `collectModulePreviews()` / `collectAllModulePreviews()` call sites.
 */
class PreviewLabIrGenerationExtension(
    private val config: PluginConfig,
    private val messageCollector: MessageCollector = MessageCollector.NONE,
) : IrGenerationExtension {

    companion object {
        private val CMP_PREVIEW_FQ = FqName("org.jetbrains.compose.ui.tooling.preview.Preview")
        private val ANDROID_PREVIEW_FQ = FqName("androidx.compose.ui.tooling.preview.Preview")
        private val CPL_OPTION_FQ = FqName("me.tbsten.compose.preview.lab.ComposePreviewLabOption")
    }

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val previews = collectPreviews(moduleFragment)
        val compatContext = CompatContext.load()
        val bodyFiller =
            PreviewLabIrBodyFiller(pluginContext, config, moduleFragment, previews, compatContext, messageCollector)
        compatContext.transformModuleFragment(moduleFragment, bodyFiller)

        // Fill bodies into the marker classes / hint functions emitted by
        // `PreviewLabHintFirGenerator`. Bodies cannot be generated at the FIR layer, so the FIR
        // generator hands the IR pass empty constructors / functions and we materialize them
        // here. Skipping this step makes the JVM backend assert with
        // `Function has no body: CONSTRUCTOR GENERATED[Keys.PreviewLabHintMarker]`.
        if (compatContext.supportsKlibCrossModuleHint()) {
            compatContext.transformModuleFragment(
                moduleFragment,
                PreviewLabHintIrBodyFiller(pluginContext, compatContext, previews, config),
            )
        }

        // Auto-provider emission.
        //
        // - Kotlin 2.3.21+ (any platform): `PreviewLabHintFirGenerator` declared one
        //   `previewLabAutoProvider_<hash>` stub alongside each marker / hint via FIR's
        //   standard declaration pipeline; `PreviewLabHintIrBodyFiller` (above) just filled
        //   the body. Going through FIR is what gives the function a proper KLIB IdSignature
        //   that consumers can resolve. The leaf-only emission gate in
        //   `PreviewLabHintEntries.compute` ensures exactly one provider per Kotlin module
        //   compile, even for KMP modules with multiple source-set sessions.
        // - Older Kotlin (JVM only via the existing version gate): no FIR hint runs, so we
        //   fall back to the legacy IR-based path that emits both the provider and a
        //   `previewLabExport(PreviewExport)` hint. Conditioned on `!bodyFiller.didGenerateAnyHint`
        //   and `previews.isNotEmpty()` to keep the previous behaviour intact.
        if (!compatContext.supportsKlibCrossModuleHint() &&
            previews.isNotEmpty() &&
            !bodyFiller.didGenerateAnyHint &&
            pluginContext.platform?.isJvm() == true
        ) {
            val sourceFile = previews.first().function.file
            val legacyHash = computeAutoProviderName(moduleFragment, config).asString()
                .removePrefix(AutoProviderPrefix)
            GenerateAutoPreviewExport(pluginContext, moduleFragment, compatContext, previews, config)
                .invoke(sourceFile, legacyHash)
        }
    }

    private fun collectPreviews(moduleFragment: IrModuleFragment): List<PreviewFunctionInfo> {
        val result = mutableListOf<PreviewFunctionInfo>()
        for (file in moduleFragment.files) {
            for (decl in file.declarations) {
                if (decl !is IrSimpleFunction) continue
                buildPreviewInfo(decl)?.let { result.add(it) }
            }
        }
        return result.sortedBy { it.displayName }
    }

    /**
     * Builds a [PreviewFunctionInfo] for a `@Preview`-annotated top-level function, or returns
     * null if the function should be skipped (not annotated, or `ignore = true`).
     *
     * **Input**: `@Preview fun MyButton()` in `src/main/kotlin/com/example/MyButton.kt`
     *
     * **Output**:
     * ```kotlin
     * PreviewFunctionInfo(
     *     function = <IrSimpleFunction for MyButton>,
     *     id = "com.example.MyButton",
     *     displayName = "com.example.MyButton",
     *     filePath = "src/main/kotlin/com/example/MyButton.kt",
     *     startLineNumber = 10,
     *     endLineNumber = 15,
     *     code = "{ ... }",
     *     kdoc = null,
     * )
     * ```
     *
     * With `@ComposePreviewLabOption(displayName = "My Button", id = "custom-id")`:
     * ```kotlin
     * PreviewFunctionInfo(id = "custom-id", displayName = "My Button", ...)
     * ```
     *
     * Template placeholders `{{package}}`, `{{simpleName}}`, `{{qualifiedName}}` are resolved
     * in both `displayName` and `id`.
     */
    private fun buildPreviewInfo(func: IrSimpleFunction): PreviewFunctionInfo? {
        if (!func.hasAnnotationCompat(CMP_PREVIEW_FQ) && !func.hasAnnotationCompat(ANDROID_PREVIEW_FQ)) return null

        val optionAnno = func.getAnnotationCompat(CPL_OPTION_FQ)
        val ignore = (optionAnno?.arguments?.getOrNull(1) as? IrConst)?.value as? Boolean ?: false
        if (ignore) return null

        val packageName = func.file.packageFqName.asString()
        val simpleName = func.name.asString()
        val qualifiedName = if (packageName.isEmpty()) simpleName else "$packageName.$simpleName"

        fun resolve(template: String) = template
            .replace("{{package}}", packageName)
            .replace("{{simpleName}}", simpleName)
            .replace("{{qualifiedName}}", qualifiedName)

        val displayName = resolve(
            (optionAnno?.arguments?.getOrNull(0) as? IrConst)?.value as? String ?: "{{qualifiedName}}",
        )
        val id = resolve(
            (optionAnno?.arguments?.getOrNull(2) as? IrConst)?.value as? String ?: "{{qualifiedName}}",
        )

        val fileEntry = func.file.fileEntry
        val rawPath = fileEntry.name
        val filePath = config.projectRootPath?.let { root ->
            runCatching {
                java.nio.file.Paths.get(root).relativize(java.nio.file.Paths.get(rawPath)).toString()
            }.getOrNull()
        } ?: rawPath
        val startLineNumber = fileEntry.getLineNumber(func.startOffset) + 1
        val endLineNumber = fileEntry.getLineNumber(func.endOffset) + 1

        val (code, kdoc) = extractSourceText(func)

        return PreviewFunctionInfo(func, id, displayName, filePath, startLineNumber, endLineNumber, code, kdoc)
    }
}
