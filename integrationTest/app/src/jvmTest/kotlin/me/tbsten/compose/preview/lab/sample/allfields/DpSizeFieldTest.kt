@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.kotest.property.Arb
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.field

@OptIn(ExperimentalTestApi::class)
class DpSizeFieldTest {
    @Test
    fun `DpSizeField should update button size when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { DpSizeFieldExample() } }

        val buttonSizeField = state.field<DpSize>("Button Size")

        forAll(Arb.float(50f..200f).map { DpSize(it.dp, (it / 2).dp) }.plusEdgecases(buttonSizeField.testValues())) { size ->
            buttonSizeField.value = size
            awaitIdle()
            true
        }
    }
}
