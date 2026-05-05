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
        /** `me.tbsten.compose.preview.lab.collectModulePreviews` sentinel call FQN。 */
        val COLLECT_MODULE_PREVIEWS_FQN: FqName =
            FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "collectModulePreviews"))

        /** `me.tbsten.compose.preview.lab.collectAllModulePreviews` sentinel call FQN。 */
        val COLLECT_ALL_MODULE_PREVIEWS_FQN: FqName =
            FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "collectAllModulePreviews"))

        /** `me.tbsten.compose.preview.lab.CollectedPreview` `ClassId` — hint 関数の戻り値型。 */
        val COLLECTED_PREVIEW_CLASS_ID: ClassId = ClassId(
            FqName("me.tbsten.compose.preview.lab"),
            Name.identifier("CollectedPreview"),
        )

        /** Per-declaration hint 関数 / marker interface が住む package。 */
        val HINT_PACKAGE: FqName = FqName("me.tbsten.compose.preview.lab.hints")

        /** Per-declaration hint 関数名 (固定)。 marker class param で IdSignature を per-`@Preview` 区別する。 */
        val HINT_FUNCTION_NAME: Name = Name.identifier("previewHint")

        /** `me.tbsten.compose.preview.lab.hints/previewHint`。 */
        val HINT_FUNCTION_CALLABLE_ID: CallableId = CallableId(HINT_PACKAGE, HINT_FUNCTION_NAME)

        /** Per-`@Preview` marker interface 名 prefix。 suffix は canonical key の sha256。 */
        const val PreviewHintMarkerPrefix: String = "PreviewHintMarker_"

        /** CMP `@Preview` annotation FQN (predicate registration 用)。 */
        val CMP_PREVIEW_ANNOTATION_FQN: FqName =
            FqName("org.jetbrains.compose.ui.tooling.preview.Preview")

        /** Android `@Preview` annotation FQN (predicate registration 用)。 */
        val ANDROID_PREVIEW_ANNOTATION_FQN: FqName =
            FqName("androidx.compose.ui.tooling.preview.Preview")
    }
}

internal val FirSession.previewLabFirBuiltIns: PreviewLabFirBuiltIns by FirSession.sessionComponentAccessor()
