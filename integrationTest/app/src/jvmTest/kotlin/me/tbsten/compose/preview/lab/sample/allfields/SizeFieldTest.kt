@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.map
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class SizeFieldTest {
    @Test
    fun `SizeField should update canvas size when value changes`() = runDesktopComposeUiTest {
        forAll(
            Arb.float(10f..200f).map { Size(it, it) }
        ) { size ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { SizeFieldExample() } }

            val canvasField = state.field<Size>("Canvas")
            canvasField.value = size

            awaitIdle()
            true
        }
    }
}
