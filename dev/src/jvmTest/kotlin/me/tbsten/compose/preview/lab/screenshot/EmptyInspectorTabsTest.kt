@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.screenshot

import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.core.spec.style.StringSpec
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabTabPager
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText

/**
 * Regression: `inspectorTabs = emptyList()` (documented way to hide every inspector tab) must not crash while the
 * inspector panel is visible at medium width.
 */
@OptIn(ExperimentalTestApi::class, UiComposePreviewLabApi::class)
class EmptyInspectorTabsTest : StringSpec({
    "PreviewLabTabPager renders with no tabs" {
        runDesktopComposeUiTest(width = 400, height = 300) {
            setContent {
                PreviewLabTheme {
                    PreviewLabTabPager(
                        tabs = emptyList<String>(),
                        title = { it },
                        pagerState = rememberPagerState { 0 },
                    ) { tab -> PreviewLabText(tab) }
                }
            }
            awaitIdle()
        }
    }

    "PreviewLab with empty inspectorTabs and visible inspector renders at medium width" {
        runDesktopComposeUiTest(width = 1280, height = 800) {
            setContent {
                val state = remember { PreviewLabState(initialIsInspectorPanelVisible = true) }
                ScreenshotHost(state = state) {
                    PreviewLab(
                        state = state,
                        inspectorTabs = emptyList(),
                    ) {
                        PreviewLabText("empty-inspector-tabs-content")
                    }
                }
            }
            awaitIdle()
            onNodeWithText("empty-inspector-tabs-content").assertIsDisplayed()
        }
    }
})
