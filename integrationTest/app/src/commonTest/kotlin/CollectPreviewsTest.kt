package app

import kotlin.test.Test
import kotlin.test.assertTrue

class CollectPreviewsTest {

    @Test
    fun collectModulePreviewsContainsThisModulePreviews() {
        assertTrue(appPreviews.toList().size == appModulePreviews.toList().size + 2)
    }
}
