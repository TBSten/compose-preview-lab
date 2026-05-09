package me.tbsten.compose.preview.lab.compiler.fir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * FIR session-scoped component holding plugin configuration and shared identifiers
 * (FQNs / `ClassId`s) referenced from FIR / IR extensions.
 *
 * Pattern adapted from Metro `MetroFirBuiltIns`
 * (https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/MetroFirBuiltIns.kt).
 */
internal class PreviewLabFirBuiltIns(session: FirSession, val config: PluginConfig) : FirExtensionSessionComponent(session) {

    companion object {
        /** `me.tbsten.compose.preview.lab.collectModulePreviews` sentinel call FQN. */
        val COLLECT_MODULE_PREVIEWS_FQN: FqName =
            FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "collectModulePreviews"))

        /** `me.tbsten.compose.preview.lab.collectAllModulePreviews` sentinel call FQN. */
        val COLLECT_ALL_MODULE_PREVIEWS_FQN: FqName =
            FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "collectAllModulePreviews"))

        /** `me.tbsten.compose.preview.lab.CollectedPreview` `ClassId` — return type of every hint function. */
        val COLLECTED_PREVIEW_CLASS_ID: ClassId = ClassId(
            FqName("me.tbsten.compose.preview.lab"),
            Name.identifier("CollectedPreview"),
        )

        /** Package that owns every per-declaration hint function and marker interface. */
        val HINT_PACKAGE: FqName = FqName("me.tbsten.compose.preview.lab.hints")

        /**
         * Prefix of every per-declaration hint function. Full name is `previewHint_<scope>`,
         * where `<scope>` is the value of `@ComposePreviewLabOption(collectScopes = ...)` (or
         * `"default"` when not set). Encoding the scope into the function name lets the IR
         * side filter hints purely by `referenceFunctions(CallableId(HINT_PACKAGE, name))` —
         * no per-hint inspection is needed.
         */
        const val PreviewHintFunctionPrefix: String = "previewHint_"

        /**
         * Builds the hint function callable id for a given scope. The hint function name
         * always lives in [HINT_PACKAGE], so per-scope discovery only needs to vary the
         * callable name suffix.
         */
        fun hintFunctionCallableId(scope: String): CallableId =
            CallableId(HINT_PACKAGE, Name.identifier(PreviewHintFunctionPrefix + scope))

        /**
         * Validation rule for `collectScope` values.
         *
         * The scope is embedded directly into a Kotlin identifier (`previewHint_<scope>`),
         * so anything outside `[A-Za-z0-9_]+` would either fail to compile or, worse,
         * accidentally land on an unrelated function name. Validating up front lets us
         * surface the mistake as a clear `MessageCollector` error instead.
         */
        val SCOPE_VALIDATION_REGEX: Regex = Regex("[A-Za-z0-9_]+")

        /**
         * Prefix of the per-`@Preview` marker interface name. Full name is
         * `PreviewHintMarker_<sanitized_fqn>_<hash>` where `<sanitized_fqn>` is the source
         * FQN with `.` replaced by `_` (debugging aid) and `<hash>` is the canonical-key
         * sha256 ([HashLength] chars, used for overload disambiguation and as the
         * cross-FIR/IR matching key).
         */
        const val PreviewHintMarkerPrefix: String = "PreviewHintMarker_"

        /** Length of the hash suffix on marker / hint declarations (matches `computeHintHash`). */
        const val HashLength: Int = 8

        /** CMP `@Preview` annotation FQN (used for predicate registration). */
        val CMP_PREVIEW_ANNOTATION_FQN: FqName =
            FqName("org.jetbrains.compose.ui.tooling.preview.Preview")

        /** Android `@Preview` annotation FQN (used for predicate registration). */
        val ANDROID_PREVIEW_ANNOTATION_FQN: FqName =
            FqName("androidx.compose.ui.tooling.preview.Preview")

        /**
         * `me.tbsten.compose.preview.lab.ComposePreviewLabOption` FQN — used both for FIR
         * `LookupPredicate` registration (eagerly resolves the annotation type ref) and as
         * the source of [COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID].
         */
        val COMPOSE_PREVIEW_LAB_OPTION_FQN: FqName =
            FqName("me.tbsten.compose.preview.lab.ComposePreviewLabOption")

        /**
         * `me.tbsten.compose.preview.lab.ComposePreviewLabOption` `ClassId` — looked up on
         * each `@Preview` symbol to read user-supplied options (e.g. `ignore = true`) during
         * hint emission.
         */
        val COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID: ClassId =
            ClassId.topLevel(COMPOSE_PREVIEW_LAB_OPTION_FQN)

        /** `@ComposePreviewLabOption(ignore = ...)` argument name. */
        val IGNORE_NAME: Name = Name.identifier("ignore")

        /** `@ComposePreviewLabOption(collectScopes = ...)` argument name. */
        val COLLECT_SCOPE_NAME: Name = Name.identifier("collectScopes")
    }
}

internal val FirSession.previewLabFirBuiltIns: PreviewLabFirBuiltIns by FirSession.sessionComponentAccessor()
