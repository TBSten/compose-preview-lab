package app

import kotlin.test.Test
import kotlin.test.assertTrue

class CollectPreviewsTest {

    @Test
    fun collectModulePreviewsContainsThisModulePreviews() {
        assertTrue(appPreviews.size == appModulePreviews.size + 2)
    }
}
