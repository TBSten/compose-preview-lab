@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class SelectableFieldTest : StringSpec({

    "SelectableField should update theme when selection changes" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { SelectableFieldExample() } }

            val themeField by state.field<String>("Theme")

            forAll(Arb.of(themeField.testValues()).plusEdgecases(themeField.testValues())) { theme ->
                themeField.value = theme
                awaitIdle()

                onNodeWithText("current theme: $theme")
                    .isDisplayed()
            }

            awaitIdle()
        }
    }
})
