package app

import kotlin.test.Test
import kotlin.test.assertTrue

class CollectPreviewsTest {

    @Test
    fun collectModulePreviewsContainsThisModulePreviews() {
        assertTrue(
            appModulePreviews.isNotEmpty(),
            "collectModulePreviews() should collect at least one @Preview from this module",
        )
    }
}
