@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.getAnnotationCompat
import me.tbsten.compose.preview.lab.compiler.compat.hasAnnotationCompat
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.kotlinFqName
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

        // Body filler for the per-declaration hints. Fills the body of every
        // `previewHint(value: PreviewHintMarker_<hash>?): CollectedPreview` emitted by the
        // FIR generator (the function name is fixed to `previewHint`; the hash is
        // recovered from the marker class short name).
        //
        // Since the FIR generator skips `@ComposePreviewLabOption(ignore = true)` previews
        // before emitting any hint declaration, we must NOT include ignored previews in the
        // hash → preview map either: doing so would let an ignored preview's hash sit in
        // the map without a corresponding emitted hint, raising the chance that a future
        // emitted preview's truncated SHA-256 hash collides with an ignored preview's hash
        // and produces a false-positive collision ERROR. `collectPreviews` already excludes
        // ignored entries.
        if (compatContext.supportsKlibCrossModuleHint()) {
            val previewsByHash = buildPreviewByHashMap(previews) { hash, existing, conflicting ->
                val existingSignature = existing.function.canonicalSignatureForReport()
                val conflictingSignature = conflicting.function.canonicalSignatureForReport()
                val message = "[ComposePreviewLab] hint hash collision detected on `$hash`. " +
                    "Two distinct @Preview functions hash to the same value: " +
                    "`$existingSignature` and `$conflictingSignature`. " +
                    "This is astronomically rare (~10⁻⁷ at 1k previews) but indicates a SHA-256 " +
                    "truncation collision. Workaround: rename one of the functions or its package."
                // Report at the *new* (conflicting) function's location so the build log points at
                // the second @Preview that triggered the collision; the first one is named in the
                // message body.
                messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    message,
                    conflicting.function.compilerMessageLocation(),
                )
            }
            compatContext.transformModuleFragment(
                moduleFragment,
                PreviewHintIrBodyFiller(pluginContext, compatContext, previewsByHash),
            )
        }
        // On older Kotlin (<2.3.21) the hint generator is not active, so
        // collectAllModulePreviews() cannot perform cross-module aggregation.
        // PreviewLabIrBodyFiller.reportUnsupportedCollectAllError detects the
        // `val by collectAllModulePreviews()` by-delegate pattern in the IR phase and
        // surfaces a compile-time error via MessageCollector.
        // collectModulePreviews() on its own only injects this module's previews via
        // an IR transform, so it works without a version gate.
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
    private fun buildPreviewInfo(func: IrSimpleFunction): PreviewFunctionInfo? =
        buildPreviewInfoOrNull(func, includeIgnored = false)

    /**
     * Internal variant of [buildPreviewInfo]. When [includeIgnored] = true, returns a
     * [PreviewFunctionInfo] even for `ignore = true` previews, instead of dropping them.
     * Used by the per-declaration hint body filler so that hint bodies for
     * `@ComposePreviewLabOption(ignore = true)` previews can still be filled in.
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

/**
 * Builds a human-readable signature in the form `<sourceFqn>(<paramType1>, <paramType2>, ...)`.
 *
 * Used in hash-collision error reports where same-name overloads must be told apart.
 * Carries the same information as the canonical key fed into the hash, but uses
 * `, ` instead of `,` as the separator to read more naturally.
 */
private fun IrSimpleFunction.canonicalSignatureForReport(): String {
    val params = parameters.filter { it.kind == IrParameterKind.Regular }.joinToString(", ") { p ->
        val classFqn = p.type.classFqName?.asString() ?: "?"
        if (p.type.isMarkedNullable()) "$classFqn?" else classFqn
    }
    return "${kotlinFqName.asString()}($params)"
}

/**
 * Build a [CompilerMessageLocation] pointing at the function's declaration site so that
 * `MessageCollector.report` produces a clickable location in IDE / CI logs.
 */
private fun IrSimpleFunction.compilerMessageLocation(): CompilerMessageLocation? {
    val fileEntry = file.fileEntry
    val line = fileEntry.getLineNumber(startOffset) + 1
    val column = fileEntry.getColumnNumber(startOffset) + 1
    return CompilerMessageLocation.create(fileEntry.name, line, column, lineContent = null)
}
