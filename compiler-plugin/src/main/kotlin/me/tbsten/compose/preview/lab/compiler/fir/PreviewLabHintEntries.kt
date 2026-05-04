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
        // **Leaf-only emission** for KMP modules. K2 runs a separate FirSession for every
        // source-set fragment a platform compile sees: `<commonMain>` (root), intermediates
        // such as `<webMain>` (jsAndWasm shared), and the platform-leaf such as
        // `<Compose-Preview-Lab-Integration-Test:uiLib>` (named with the full Gradle project
        // path + module name). All sessions narrow `componentPlatforms` to the same single
        // target during a per-platform compile, so the leaf signal is **the name shape
        // itself**: only the leaf session's `moduleData.name` reflects the Gradle module
        // identity — non-leaf sessions use bare source-set names (`<commonMain>`,
        // `<webMain>`, `<jsMain>`, `<jvmMain>`, `<iosMain>`, etc.) without any project /
        // module disambiguator.
        //
        // Without this gate, each KMP session's `getTopLevelClassIds()` emits its own
        // `PreviewLabExportMarker_<hashN>`. The IR pass runs once on the merged module
        // fragment (named after the leaf) and can only emit a single
        // `previewLabAutoProvider_<leafHash>`. The non-leaf markers' hint functions would
        // then point at provider FQNs that never get materialised, surfacing downstream as
        // `IrLinkageError: No function found for symbol previewLabAutoProvider_<hash>` at
        // KLIB link time.
        //
        // Single-target non-KMP modules (pure Kotlin/JVM) only have one source-set session,
        // and its name carries the Gradle module identity by default — so `isKmpSourceSetFragmentName`
        // returns false and emission proceeds.
        if (session.moduleData.name.asString().isKmpSourceSetFragmentName()) return emptyList()

        // SHA-256-based hash to avoid Java `String.hashCode()` collisions on user-controlled
        // module names (e.g. `"Aa".hashCode() == "BB".hashCode()`); the project root path
        // disambiguates two unrelated published artifacts that happen to share a Kotlin module
        // name (each was built by its own Gradle invocation, so the path differs). See
        // [computeModuleHash] for the reasoning.
        val moduleNameHash = computeModuleHash(
            moduleName = session.moduleData.name.asString(),
            disambiguator = session.previewLabFirBuiltIns.config.projectRootPath,
        )
        val markerClassId = ClassId(
            PreviewLabFirBuiltIns.HINT_PACKAGE,
            Name.identifier("$MarkerClassPrefix$moduleNameHash"),
        )
        return listOf(HintEntry(markerClassId))
    }

    companion object {
        const val MarkerClassPrefix = "PreviewLabExportMarker_"

        /**
         * Recognises the bare source-set fragment names K2 uses for non-leaf KMP sessions
         * (`<commonMain>`, `<webMain>`, `<jsMain>`, …). The leaf session's `moduleData.name`
         * always carries the Gradle module identity (project path / module name) and never
         * matches this set, so the negation is what we filter emission on.
         *
         * The list mirrors the standard KMP source-set DSL plus common intermediate names.
         * If a future Kotlin version adds a new shared source-set name shape, add it here.
         */
        private val KMP_SOURCE_SET_FRAGMENT_NAMES = setOf(
            "commonMain", "commonTest",
            "jvmMain", "jvmTest",
            "androidMain", "androidUnitTest", "androidInstrumentedTest",
            "jsMain", "jsTest",
            "wasmJsMain", "wasmJsTest",
            "wasmWasiMain", "wasmWasiTest",
            "nativeMain", "nativeTest",
            "linuxMain", "linuxTest",
            "macosMain", "macosTest",
            "iosMain", "iosTest",
            "iosSimulatorArm64Main", "iosSimulatorArm64Test",
            "iosArm64Main", "iosArm64Test",
            "iosX64Main", "iosX64Test",
            "tvosMain", "tvosTest",
            "watchosMain", "watchosTest",
            "mingwMain", "mingwTest",
            "webMain", "webTest",
            "appleMain", "appleTest",
            "concurrentMain", "concurrentTest",
        )

        private fun String.isKmpSourceSetFragmentName(): Boolean {
            val inner = if (startsWith('<') && endsWith('>')) substring(1, length - 1) else this
            return inner in KMP_SOURCE_SET_FRAGMENT_NAMES
        }
    }
}
