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
         * Fixed callable name shared by every per-declaration hint function. The marker class
         * parameter type disambiguates the IdSignature per `@Preview`.
         */
        val HINT_FUNCTION_NAME: Name = Name.identifier("previewHint")

        /** `me.tbsten.compose.preview.lab.hints/previewHint`. */
        val HINT_FUNCTION_CALLABLE_ID: CallableId = CallableId(HINT_PACKAGE, HINT_FUNCTION_NAME)

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
    }
}

internal val FirSession.previewLabFirBuiltIns: PreviewLabFirBuiltIns by FirSession.sessionComponentAccessor()
