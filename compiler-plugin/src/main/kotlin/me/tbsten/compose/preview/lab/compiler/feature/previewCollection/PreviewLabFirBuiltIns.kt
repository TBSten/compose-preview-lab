package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent

/**
 * FIR session-scoped component holding plugin configuration shared between FIR extensions.
 *
 * Pure-data identifiers (FQNs / `ClassId` / `CallableId` / `Name` / `Regex` / pure
 * functions that do not need a `FirSession`) live next to their respective logics
 * inside `feature/previewCollection/` (e.g. [HINT_PACKAGE],
 * [COLLECTED_PREVIEW_CLASS_ID], [PreviewAnnotationPredicates]) — anything reachable via
 * plain `import` belongs there, not on this session component. This class is reserved
 * for session-bound state (currently just [config]; session-scoped caches such as
 * [HintEntriesProvider] are registered as their own `FirExtensionSessionComponent`).
 *
 * Pattern adapted from Metro `MetroFirBuiltIns`
 * (https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/MetroFirBuiltIns.kt).
 */
internal class PreviewLabFirBuiltIns(session: FirSession, val config: PluginConfig) : FirExtensionSessionComponent(session)

internal val FirSession.previewLabFirBuiltIns: PreviewLabFirBuiltIns by FirSession.sessionComponentAccessor()
