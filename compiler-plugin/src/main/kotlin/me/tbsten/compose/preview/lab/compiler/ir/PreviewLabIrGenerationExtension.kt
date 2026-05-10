@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.ir

import me.tbsten.compose.preview.lab.ComposePreviewLabOption
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
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrVararg
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
        // The hint body filler runs whenever the FIR per-declaration generator is
        // registered (= 2.3.20+ via `supportsFirHintGeneration`). On JVM / Android the
        // FIR-emitted markers + hints become classfiles and the body filler injects the
        // `CollectedPreview(...)` constructor call into each hint at IR time. On KLIB
        // targets the same body filler runs, but the consumer-side cross-module
        // discovery is additionally gated on `supportsKlibCrossModuleHint` (KT-82395
        // fix) inside `HintDiscovery` / `PreviewLabIrBodyFiller`.
        if (compatContext.supportsFirHintGeneration()) {
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
        // On Kotlin <2.3.20 the FIR hint generator is not registered (the FIR top-level
        // declaration generation API was experimental on 2.3.0–2.3.19 and stabilized in
        // 2.3.20 — see `compat-k2320`), so collectAllModulePreviews() cannot perform
        // cross-module aggregation. PreviewLabIrBodyFiller.reportUnsupportedCollectAllError
        // detects the `val by collectAllModulePreviews()` by-delegate pattern in the IR
        // phase and surfaces a compile-time error via MessageCollector. On Kotlin 2.3.20
        // (= the FIR gate is open) for KLIB targets, the body filler also reports the
        // unsupported-platform error because the KLIB IC safety fix (KT-82395) only
        // landed in 2.3.21. collectModulePreviews() on its own only injects this
        // module's previews via an IR transform, so it works without a version gate.
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
        val ignore = (optionAnno?.findArgumentByName("ignore") as? IrConst)?.value as? Boolean ?: false
        if (ignore && !includeIgnored) return null

        val packageName = func.file.packageFqName.asString()
        val simpleName = func.name.asString()
        val qualifiedName = if (packageName.isEmpty()) simpleName else "$packageName.$simpleName"

        fun resolve(template: String) = template
            .replace("{{package}}", packageName)
            .replace("{{simpleName}}", simpleName)
            .replace("{{qualifiedName}}", qualifiedName)

        val displayName = resolve(
            (optionAnno?.findArgumentByName("displayName") as? IrConst)?.value as? String ?: "{{qualifiedName}}",
        )
        val id = resolve(
            (optionAnno?.findArgumentByName("id") as? IrConst)?.value as? String ?: "{{qualifiedName}}",
        )
        // Substitute the `DefaultCollectScope` sentinel with the module's configured
        // `defaultCollectScope` Gradle DSL value, the same way `PreviewLabIrBodyFiller`
        // does for the *requested* scope on the call site. Without this, a module that
        // sets `defaultCollectScope = "acme_ui"` would have its `collectModulePreviews()`
        // resolve to `"acme_ui"` (substituted) but its unannotated previews stay pinned
        // to `["default"]`, so the in-module filter `scope in scopes` ends up empty.
        val rawScopes = readScopesArgument(optionAnno?.findArgumentByName("collectScopes"))
        val scopes = rawScopes.map {
            if (it == ComposePreviewLabOption.DefaultCollectScope) config.defaultCollectScope else it
        }

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

        return PreviewFunctionInfo(func, id, displayName, filePath, startLineNumber, endLineNumber, code, kdoc, scopes)
    }
}

/**
 * Looks up a named argument off an `@ComposePreviewLabOption(...)` IR annotation by
 * walking the annotation constructor's value parameters and using the matching index
 * into [IrConstructorCall.arguments]. Prefer this over `arguments.getOrNull(<int>)` so
 * that adding / reordering annotation parameters does not silently shift the readout
 * onto an unrelated value.
 *
 * **Sample call → result**:
 * ```kotlin
 * // @ComposePreviewLabOption(displayName = "X", ignore = false, id = "Y", collectScopes = ["d"])
 * optionAnno.findArgumentByName("collectScopes")  // → IrVararg [IrConst("d")]
 * optionAnno.findArgumentByName("ignore")        // → IrConst(false)
 *
 * // @ComposePreviewLabOption(displayName = "X")  ← collectScope omitted, default applies
 * optionAnno.findArgumentByName("collectScopes")  // → IrVararg [IrConst("default")]
 *                                                //   (the annotation's declared default)
 *
 * optionAnno.findArgumentByName("missing")       // → null  (no such parameter)
 * ```
 *
 * **`arguments[i]` is `null` for omitted-with-default arguments**: when the call site
 * leaves a parameter unspecified, the slot in `arguments` is `null` rather than being
 * pre-filled with the declared default. For annotation classes Kotlin always forces
 * default-value IR conversion (`forcedDefaultValueConversion = true` in
 * `Fir2IrConverter.createIrParameter`, plus deserialization via
 * `annotationParameterDefaultValue` since 2.2.0) so `parameters[i].defaultValue?.expression`
 * is reliably populated for both source-side and binary-loaded annotations. We fall back
 * to it here so callers always observe the declared default IR — without this fallback,
 * the IR side would have to hard-code a duplicate default per parameter (fragile: any
 * annotation default change would silently disagree with the compiler-plugin readout).
 *
 * Returns `null` only when the annotation does not declare a parameter with [name].
 */
private fun IrConstructorCall.findArgumentByName(name: String): IrExpression? {
    val parameters = symbol.owner.parameters
    val index = parameters.indexOfFirst { it.kind == IrParameterKind.Regular && it.name.asString() == name }
    if (index < 0) return null
    return arguments.getOrNull(index) ?: parameters[index].defaultValue?.expression
}

/**
 * Reads the `Array<String>` annotation argument for `collectScope` off the IR
 * representation of `@ComposePreviewLabOption(collectScopes = [...])`.
 *
 * **Sample call → result**:
 * ```kotlin
 * // @ComposePreviewLabOption(collectScopes = ["design", "showcase"])
 * readScopesArgument(<IrVararg of IrConst("design"), IrConst("showcase")>)
 *   → ["design", "showcase"]
 *
 * // No annotation, or argument absent
 * readScopesArgument(null) → ["default"]
 * ```
 *
 * Annotation `Array<String>` arguments arrive as `IrVararg` whose elements are
 * `IrConst<String>` — both for source-side `@ComposePreviewLabOption(collectScopes = [...])`
 * usages (`Fir2IrVisitor.convertToArrayLiteral`) and for usages read from compiled
 * dependencies (`IrBodyDeserializer` for KLIB / .class). This was empirically verified by
 * gating the previous IrConst / `else` fallback branches with `error(...)` and re-running
 * the entire `:compiler-plugin:test` suite (multi-Kotlin matrix included) — neither branch
 * was ever hit. Anything that is not an `IrVararg` (no annotation, absent argument,
 * synthetic IR built outside a real compilation, ...) falls back to the `["default"]`
 * default rather than crashing the build.
 */
private fun readScopesArgument(arg: IrExpression?): List<String> {
    // Note: `Array<String>` annotation arguments always lower to `IrVararg` (both for
    // source-side and dependency-binary-side annotations). `IrConst` only carries
    // primitive values, so it can never hold an `Array<String>` directly — the vararg
    // shape is the only one we need to recognise here. Verified empirically by gating
    // an `is IrConst -> error(...)` branch and re-running the entire
    // `:compiler-plugin:test` matrix without a single hit.
    val vararg = arg as? IrVararg ?: return listOf(ComposePreviewLabOption.DefaultCollectScope)
    return vararg.elements
        .mapNotNull { (it as? IrConst)?.value as? String }
        .ifEmpty { listOf(ComposePreviewLabOption.DefaultCollectScope) }
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
