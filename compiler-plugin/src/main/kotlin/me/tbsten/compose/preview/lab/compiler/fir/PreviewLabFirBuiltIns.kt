package me.tbsten.compose.preview.lab.compiler.fir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * FIR session-scoped component that holds plugin configuration and shared identifiers
 * (FQNs / `ClassId`s / `CallableId`s) used across the FIR-side extensions.
 *
 * Pattern adapted from Metro `MetroFirBuiltIns`
 * (https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/MetroFirBuiltIns.kt).
 *
 * Will be registered via `FirExtensionRegistrarContext.+::PreviewLabFirBuiltIns` once the
 * FIR-side hint generator is wired up (planned in a follow-up PR), so other FIR extensions
 * can resolve it via `session.previewLabFirBuiltIns`. Currently this class only holds shared
 * identifiers and is referenced from the IR side.
 */
internal class PreviewLabFirBuiltIns(session: FirSession, val config: PluginConfig,) : FirExtensionSessionComponent(session) {

    companion object {
        /** Package that owns every plugin-generated declaration (marker classes + hint functions). */
        val HINT_PACKAGE: FqName = FqName("me.tbsten.compose.preview.lab.exports")

        /** Fixed callable name shared by every hint function (parameter type disambiguates them). */
        val HINT_FUNCTION_NAME: Name = Name.identifier("previewLabExport")

        /** Fixed value-parameter name on every hint function. */
        val HINT_VALUE_PARAM_NAME: Name = Name.identifier("value")

        /** `me.tbsten.compose.preview.lab.exports/previewLabExport`. */
        val HINT_FUNCTION_CALLABLE_ID: CallableId = CallableId(HINT_PACKAGE, HINT_FUNCTION_NAME)

        /** `me.tbsten.compose.preview.lab.PreviewExportHint` annotation FQN. */
        val PREVIEW_EXPORT_HINT_FQN: FqName =
            FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "PreviewExportHint"))

        /** `me.tbsten.compose.preview.lab.PreviewExportHint` `ClassId`. */
        val PREVIEW_EXPORT_HINT_CLASS_ID: ClassId = ClassId.topLevel(PREVIEW_EXPORT_HINT_FQN)

        /** `me.tbsten.compose.preview.lab.PreviewExport` FQN — return type of the sentinel calls. */
        val PREVIEW_EXPORT_FQN: FqName =
            FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "PreviewExport"))

        /** `me.tbsten.compose.preview.lab.collectModulePreviews` sentinel call FQN. */
        val COLLECT_MODULE_PREVIEWS_FQN: FqName =
            FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "collectModulePreviews"))

        /** `me.tbsten.compose.preview.lab.collectAllModulePreviews` sentinel call FQN. */
        val COLLECT_ALL_MODULE_PREVIEWS_FQN: FqName =
            FqName.fromSegments(listOf("me", "tbsten", "compose", "preview", "lab", "collectAllModulePreviews"))
    }
}

internal val FirSession.previewLabFirBuiltIns: PreviewLabFirBuiltIns by FirSession.sessionComponentAccessor()
