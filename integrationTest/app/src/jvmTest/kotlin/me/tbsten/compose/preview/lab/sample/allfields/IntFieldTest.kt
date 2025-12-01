@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class IntFieldTest {
    @Test
    fun `IntField should update Counter when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { IntFieldExample() } }

        val countField = state.field<Int>("Count")

        forAll(Arb.int().plusEdgecases(countField.testValues())) { intValue ->
            countField.value = intValue
            awaitIdle()

            onNodeWithText("Count: $intValue")
                .assertExists()
            true
        }
    }
}
