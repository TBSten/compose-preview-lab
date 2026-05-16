package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent

/**
 * Session-bound accessor for the version-specific [CompatContext] instance loaded once
 * at plugin entry (`ComposePreviewLabCompilerPluginRegistrar`).
 *
 * Registered as the *first* component in [me.tbsten.compose.preview.lab.compiler.PreviewLabFirExtensionRegistrar]
 * so every later FIR component / extension can resolve `session.compatContext`
 * without having to thread the value through each constructor. Pattern adapted from
 * [me.tbsten.compose.preview.lab.compiler.feature.previewCollection.HintEntriesProvider].
 *
 * **Sample access** (inside any FIR extension that has `session` in scope):
 * ```kotlin
 * session.compatContext.getDeprecationsProviderCompat(declaration, session)
 * ```
 */
internal class CompatContextSessionComponent(session: FirSession, val value: CompatContext,) :
    FirExtensionSessionComponent(session)

internal val FirSession.compatContext: CompatContext
    get() = compatContextSessionComponent.value

private val FirSession.compatContextSessionComponent: CompatContextSessionComponent
    by FirSession.sessionComponentAccessor()
