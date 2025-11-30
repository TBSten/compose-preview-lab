@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class TransformFieldTest {
    @Test
    fun `TransformField should transform string to int`() = runDesktopComposeUiTest {
        forAll(Arb.int()) { intValue ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { TransformFieldExample() } }

            val numberField = state.field<Int>("number")
            numberField.value = intValue

            awaitIdle()

            onNodeWithText("intValue: $intValue")
                .assertExists()
            true
        }
    }
}
