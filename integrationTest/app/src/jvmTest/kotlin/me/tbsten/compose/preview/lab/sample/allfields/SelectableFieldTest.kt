@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.sample.PropertyTestBase
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class SelectableFieldTest : PropertyTestBase() {
    @Test
    fun `SelectableField should update theme when selection changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { SelectableFieldExample() } }

        val themeField by state.field<String>("Theme")

        runBlocking {
            forAll(Arb.of(themeField.testValues()).plusEdgecases(themeField.testValues())) { theme ->
                themeField.value = theme
                awaitIdle()

                onNodeWithText("current theme: $theme")
                    .isDisplayed()
            }
        }

        // Ensure all coroutines (including LaunchedEffect/snapshotFlow) are completed
        awaitIdle()
    }
}
