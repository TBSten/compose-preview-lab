package app

import kotlin.test.Test
import kotlin.test.assertTrue

class CollectPreviewsTest {

    @Test
    fun collectModulePreviewsContainsThisModulePreviews() {
        // uiLib currently exposes 3 @Preview functions (MyButtonPreview, MyTextFieldPreview,
        // UserCardAutoFieldEventPreview). Adding / removing a preview in uiLib requires
        // updating this number alongside.
        assertTrue(appPreviews.toList().size == appModulePreviews.toList().size + 3)
    }
}
