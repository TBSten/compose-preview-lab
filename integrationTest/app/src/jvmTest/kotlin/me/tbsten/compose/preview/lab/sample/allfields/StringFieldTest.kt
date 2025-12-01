@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class StringFieldTest {
    @Test
    fun `StringField should update button text when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { StringFieldExample() } }

        val textField = state.field<String>("text")

        forAll(Arb.string(1..50).plusEdgecases(textField.testValues())) { stringValue ->
            textField.value = stringValue
            awaitIdle()

            onAllNodesWithText(stringValue)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
