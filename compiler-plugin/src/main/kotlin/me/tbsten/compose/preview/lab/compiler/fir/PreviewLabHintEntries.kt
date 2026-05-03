package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

/**
 * A single hint to emit, identified by the marker class id that disambiguates it from
 * sibling hints from other modules.
 *
 * **One entry per compilation unit**. The downstream consumer
 * [me.tbsten.compose.preview.lab.compiler.ir.PreviewListIrBuilder.collectDependencyGetters]
 * reads the marker class short name, strips the `PreviewLabExportMarker_` prefix, and
 * reconstructs the auto-provider FQN as `<HINT_PACKAGE>.previewLabAutoProvider_<suffix>`.
 * Both sides derive the suffix from [computeModuleHash] of the same Kotlin module name, so
 * the marker class id alone locates the matching provider — no
 * `@PreviewExportHint(fqn = ...)` annotation lookup is involved.
 *
 * Per-property `targetFqn` was considered but dropped: the auto-provider already folds in
 * both local `@Preview`s and any `collectAllModulePreviews()` deps via
 * `PreviewListIrBuilder.buildConcatenatedPreviewsExpr`, so a single per-module entry
 * carries everything downstream needs. Manual `collectModulePreviews()` /
 * `collectAllModulePreviews()` properties are user-owned and consumed locally, not
 * exported through this hint channel.
 */
internal data class HintEntry(val markerClassId: ClassId)

/**
 * Computes and caches the [HintEntry] list for one compilation unit.
 *
 * Pulled out of [PreviewLabHintFirGenerator] so the generator class only owns the
 * `FirDeclarationGenerationExtension` overrides; the rule that turns a `FirSession` into
 * the set of hints lives here.
 *
 * The cache is keyed on `Unit` because the entry list is fully determined by the session
 * (one `FirCache` instance per session, so per-session memoization is what we want).
 *
 * **For a session whose `moduleData.name` is `:uiLib` and whose hash mod-36 truncates to
 * `a3k9z2x1`** (semantically equivalent Kotlin):
 *
 * ```kotlin
 * // computed once per session, then served from cache
 * listOf(
 *     HintEntry(
 *         markerClassId = ClassId(
 *             FqName("me.tbsten.compose.preview.lab.exports"),
 *             Name.identifier("PreviewLabExportMarker_a3k9z2x1"),
 *         ),
 *     ),
 * )
 * ```
 *
 * The marker class id is derived from a hash of `session.moduleData.name` so it is unique
 * across modules on the same classpath, which is the only invariant the Step 2 KLIB
 * pipeline cares about.
 */
internal class PreviewLabHintEntries(private val session: FirSession) {

    private val cache: FirCache<Unit, List<HintEntry>, Nothing?> =
        session.firCachesFactory.createCache { _, _ -> compute() }

    fun get(): List<HintEntry> = cache.getValue(Unit, null)

    private fun compute(): List<HintEntry> {
        // SHA-256-based hash to avoid Java `String.hashCode()` collisions on user-controlled
        // module names (e.g. `"Aa".hashCode() == "BB".hashCode()`); see [computeModuleHash].
        val moduleNameHash = computeModuleHash(session.moduleData.name.asString())
        val markerClassId = ClassId(
            PreviewLabFirBuiltIns.HINT_PACKAGE,
            Name.identifier("$MarkerClassPrefix$moduleNameHash"),
        )
        return listOf(HintEntry(markerClassId))
    }

    companion object {
        const val MarkerClassPrefix = "PreviewLabExportMarker_"
    }
}
