@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class TransformFieldTest : StringSpec({

    "TransformField should transform string to int" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { TransformFieldExample() } }

            val numberField by state.field<Int>("number")

            forAll(Arb.int().plusEdgecases(numberField.testValues())) { intValue ->
                numberField.value = intValue
                awaitIdle()

                onNodeWithText("intValue: $intValue")
                    .isDisplayed()
            }

            awaitIdle()
        }
    }
})
