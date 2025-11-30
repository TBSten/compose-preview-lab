@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class ComposableFieldTest {
    @Test
    fun `ComposableField should render without error`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { ComposableFieldExample() } }
        awaitIdle()
    }

    @Test
    fun `ComposableFieldWithPredefinedValues should render without error`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { ComposableFieldWithPredefinedValuesExample() } }
        awaitIdle()
    }
}
