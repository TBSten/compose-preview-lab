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

        // Per-declaration hint (Metro 風) 用の body filler。 FIR generator が emit した
        // `previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview` の body を IR pass で埋める。
        // hint emit 側で ignore=true を filter していないため、 body fill 用の lookup は
        // ignore=true も含めて構築する。
        if (compatContext.supportsKlibCrossModuleHint()) {
            val previewsIncludingIgnored = collectPreviewsIncludingIgnored(moduleFragment)
            val previewsByHash = buildPreviewByHashMap(previewsIncludingIgnored)
            compatContext.transformModuleFragment(
                moduleFragment,
                PreviewHintIrBodyFiller(pluginContext, compatContext, previewsByHash),
            )
        }
        // 古い Kotlin (<2.3.21) では hint generator が動かないので、
        // collectAllModulePreviews() 自体が cross-module aggregation できない。
        // PreviewLabIrBodyFiller.reportUnsupportedCollectAllError が
        // `val by collectAllModulePreviews()` の by-delegate pattern を IR phase で検出して
        // compile-time error を MessageCollector 経由で報告する。
        // collectModulePreviews() 単体は IR transform で自モジュールの previews を注入する
        // だけなので version gate なしで動く。
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
     * `@Preview` annotated 関数を `@ComposePreviewLabOption(ignore = true)` も含めて全て収集する。
     * Per-declaration hint body filler は ignore=true でも body を埋める必要があるため、
     * 通常の [collectPreviews] (ignore filter 済み) ではなく本関数を使う。
     */
    private fun collectPreviewsIncludingIgnored(moduleFragment: IrModuleFragment): List<PreviewFunctionInfo> {
        val result = mutableListOf<PreviewFunctionInfo>()
        for (file in moduleFragment.files) {
            for (decl in file.declarations) {
                if (decl !is IrSimpleFunction) continue
                buildPreviewInfoOrNull(decl, includeIgnored = true)?.let { result.add(it) }
            }
        }
        return result
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
    private fun buildPreviewInfo(func: IrSimpleFunction): PreviewFunctionInfo? =
        buildPreviewInfoOrNull(func, includeIgnored = false)

    /**
     * `buildPreviewInfo` の internal 版。 [includeIgnored] = true の場合、 `ignore = true` でも
     * 除外せず [PreviewFunctionInfo] を返す。 per-declaration hint body filler が
     * `@ComposePreviewLabOption(ignore = true)` の preview にも hint body を埋めるために使う。
     */
    internal fun buildPreviewInfoOrNull(func: IrSimpleFunction, includeIgnored: Boolean): PreviewFunctionInfo? {
        if (!func.hasAnnotationCompat(CMP_PREVIEW_FQ) && !func.hasAnnotationCompat(ANDROID_PREVIEW_FQ)) return null

        val optionAnno = func.getAnnotationCompat(CPL_OPTION_FQ)
        val ignore = (optionAnno?.arguments?.getOrNull(1) as? IrConst)?.value as? Boolean ?: false
        if (ignore && !includeIgnored) return null

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
