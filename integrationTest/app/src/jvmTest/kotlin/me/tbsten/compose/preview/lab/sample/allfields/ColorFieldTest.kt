@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class ColorFieldTest {
    @Test
    fun `ColorField should update background color when value changes`() = runDesktopComposeUiTest {
        checkAll(
            listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta).exhaustive()
        ) { color ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { ColorFieldExample() } }

            val backgroundField = state.field<Color>("Background")
            backgroundField.value = color

            awaitIdle()
        }
    }
}
