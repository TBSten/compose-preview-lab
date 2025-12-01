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
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class SelectableFieldTest {
    @Test
    fun `SelectableField should update theme when selection changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { SelectableFieldExample() } }

        val themeField = state.field<String>("Theme")

        forAll(Arb.of(themeField.testValues()).plusEdgecases(themeField.testValues())) { theme ->
            themeField.value = theme
            awaitIdle()

            onNodeWithText("current theme: $theme")
                .isDisplayed()
        }
    }
}
