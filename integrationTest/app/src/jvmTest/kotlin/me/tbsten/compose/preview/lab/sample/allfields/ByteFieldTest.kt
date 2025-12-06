@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.field

@OptIn(ExperimentalTestApi::class)
class ByteFieldTest {
    @Test
    fun `ByteField should update flag when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { ByteFieldExample() } }

        val flagField = state.field<Byte>("Flag")

        forAll(Arb.byte().plusEdgecases(flagField.testValues())) { byteValue ->
            flagField.value = byteValue
            awaitIdle()

            onNodeWithText("Flag: $byteValue")
                .isDisplayed()
        }
    }
}
