package me.tbsten.compose.preview.lab.compiler.fir

import me.tbsten.compose.preview.lab.compiler.PluginConfig
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent

/**
 * FIR session-scoped component holding plugin configuration shared between FIR extensions.
 *
 * Pure-data constants (FQNs / `ClassId` / `CallableId` / `Name` / `Regex` / functions
 * that do not need a `FirSession`) live on
 * [me.tbsten.compose.preview.lab.compiler.PreviewLabConstants] instead — anything reachable
 * via plain `import` belongs there. This component is reserved for session-bound state
 * (currently just [config]; session-scoped caches such as
 * [me.tbsten.compose.preview.lab.compiler.feature.previewCollection.HintEntriesProvider] are
 * registered as their own `FirExtensionSessionComponent`).
 *
 * Pattern adapted from Metro `MetroFirBuiltIns`
 * (https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/MetroFirBuiltIns.kt).
 */
internal class PreviewLabFirBuiltIns(session: FirSession, val config: PluginConfig) : FirExtensionSessionComponent(session)

internal val FirSession.previewLabFirBuiltIns: PreviewLabFirBuiltIns by FirSession.sessionComponentAccessor()
