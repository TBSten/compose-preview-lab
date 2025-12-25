@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.sample.PBT
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class StringFieldTest : StringSpec({
    tags(PBT)

    "StringField should update button text when value changes" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { StringFieldExample() } }

            val textField by state.field<String>("text")

            forAll(Arb.string(1..50).plusEdgecases(textField.testValues())) { stringValue ->
                textField.value = stringValue
                awaitIdle()

                onAllNodesWithText(stringValue)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }

            awaitIdle()
        }
    }
})
