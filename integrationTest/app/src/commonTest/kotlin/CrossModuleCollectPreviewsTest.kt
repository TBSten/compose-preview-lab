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
     * 依存連鎖 (`app(all) → ui(all) → core`) で同 `@Preview` の `CollectedPreview` が複数の
     * hint chain を通って到達する場合があるが、 `collectAllModulePreviews()` の IR transform は
     * 最終的に `distinctPreviewsById` で dedup する。 結果 list の `id` がユニークであることを
     * assert する。
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
