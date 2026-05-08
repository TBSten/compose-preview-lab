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
     * The dependency chain (`app(all) → ui(all) → core`) can route the same `@Preview`'s
     * `CollectedPreview` through multiple hint chains. The IR transform finishes with
     * `distinctPreviewsById`, so the final list must have unique ids.
     */
    @Test
    fun collectAllModulePreviewsDeduplicatesById() {
        val ids = appPreviews.map { it.id }
        assertTrue(
            ids.size == ids.distinct().size,
            "appPreviews has id duplicates: ${ids.groupingBy { it }.eachCount().filter { it.value > 1 }}",
        )
    }

    /**
     * `@ComposePreviewLabOption(id, displayName)` overrides on a dependency-module
     * `@Preview` must round-trip through the V2 hint pipeline. `:uiLib` ships
     * `MyButtonPreview` with `id = "MyButtonPreview"` and a custom `displayName`, and
     * the consumer-side aggregation should preserve both fields verbatim.
     *
     * Without this test, a regression in
     * `PreviewLabIrGenerationExtension.buildPreviewInfoOrNull` (the option-resolution
     * step) would silently fall back to the default `{{qualifiedName}}` and the
     * gallery would lose its curated names without surfacing any compile-time signal.
     */
    @Test
    fun collectAllModulePreviewsPreservesCustomIdAndDisplayNameAcrossModules() {
        val custom = appPreviews.singleOrNull { it.id == "MyButtonPreview" }
        assertTrue(
            custom != null,
            "appPreviews missing the cross-module @ComposePreviewLabOption(id=\"MyButtonPreview\") entry. " +
                "Found ids: ${appPreviews.map { it.id }}",
        )
        assertTrue(
            custom.displayName == "UI Component in library module Preview",
            "displayName override did not round-trip: got '${custom.displayName}'",
        )
    }

    // TODO(followup-ignore-preview-cross-module-leak): once the deferred ticket lands,
    // add an assertion here that `@ComposePreviewLabOption(ignore = true)` previews from
    // dependency modules are *excluded* from `appPreviews`. Today the V2 hint emit path
    // intentionally includes ignored previews so the IR body filler can fill every hint;
    // the consumer-side filter is the missing piece. See
    // .local/ticket/followup-ignore-preview-cross-module-leak.md.
}
