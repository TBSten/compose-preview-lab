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
 * Step 2 produces exactly one entry per compilation unit. Step 3+ will extend this to
 * one entry per manual `collectModulePreviews()` / `collectAllModulePreviews()` property
 * found via `predicateBasedProvider`, plus one for the auto-export path. At that point
 * each entry will also carry the `targetFqn` that gets attached as
 * `@PreviewExportHint(fqn = ...)` on the hint function — but that wiring is intentionally
 * absent in Step 2: the FIR-emitted hints exist only to validate the KLIB pipeline, not
 * to be discovered downstream yet.
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
        val moduleNameHash = session.moduleData.name.asString().hashCode().toString(36).takeLast(8)
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
