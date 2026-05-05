package app

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Cross-module preview aggregation E2E for the bug-017 KLIB rewrite.
 *
 * `appPreviews` (delegated to `collectAllModulePreviews()`) must contain previews
 * from `:uiLib` in addition to `appModulePreviews` (the local-only collection).
 * Putting the assertion in `commonTest` makes it run on every KMP target the
 * integrationTest is wired for (JVM / Android / JS / WasmJS / iOS), so it
 * exercises the new FIR-based marker-class hint scheme on each KLIB platform
 * end-to-end (real Gradle build, real KLIB linking, real runtime evaluation).
 */
class CrossModuleCollectPreviewsTest {

    @Test
    fun collectAllModulePreviewsAggregatesDependencyModules() {
        assertTrue(
            appPreviews.isNotEmpty(),
            "collectAllModulePreviews() should return at least one preview",
        )
        assertTrue(
            appPreviews.size > appModulePreviews.size,
            "collectAllModulePreviews() (${appPreviews.size}) should include strictly " +
                "more previews than collectModulePreviews() (${appModulePreviews.size}); " +
                "missing dependency-module previews indicates the KLIB hint discovery " +
                "is broken on this target.",
        )
    }

    /**
     * 旧モジュール集約 hint と per-declaration hint (Metro 風 / T03) の両方が動いている期間中は
     * 同一 `@Preview` から `CollectedPreview` が 2 個 emit される (V1 経由 + V2 経由)。
     * `collectAllModulePreviews()` の IR transform は最終的に `distinctPreviewsById` で
     * dedup するので、 結果 list の `id` がユニークであることを assert する。
     */
    @Test
    fun collectAllModulePreviewsDeduplicatesById() {
        val ids = appPreviews.map { it.id }
        assertTrue(
            ids.size == ids.distinct().size,
            "appPreviews has id duplicates: ${ids.groupingBy { it }.eachCount().filter { it.value > 1 }}",
        )
    }
}
