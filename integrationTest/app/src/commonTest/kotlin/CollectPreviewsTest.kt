package app

import kotlin.test.Test
import kotlin.test.assertEquals

class CollectPreviewsTest {

    @Test
    fun collectModulePreviewsContainsThisModulePreviews() {
        // uiLib currently exposes 3 @Preview functions (MyButtonPreview, MyTextFieldPreview,
        // UserCardAutoFieldEventPreview). Adding / removing a preview in uiLib requires
        // updating this number alongside.
        val expectedUiLibPreviewCount = 3
        // Materialize once: `appPreviews` / `appModulePreviews` are `Sequence`s that
        // re-evaluate every per-`@Preview` factory on each `toList()`, so inlining the
        // calls inside the assertion would re-run every preview body twice.
        val allCount = appPreviews.toList().size
        val moduleCount = appModulePreviews.toList().size
        assertEquals(
            expected = moduleCount + expectedUiLibPreviewCount,
            actual = allCount,
            message = "Expected uiLib to contribute $expectedUiLibPreviewCount previews on top of " +
                "the app's $moduleCount module previews, but collectAllModulePreviews() returned " +
                "$allCount. Did you add or remove a @Preview in uiLib without updating " +
                "expectedUiLibPreviewCount?",
        )
    }
}
