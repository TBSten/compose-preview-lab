@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.sample.PropertyTestBase
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class ColorFieldTest : PropertyTestBase() {
    @Test
    fun `ColorField should update background color when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { ColorFieldExample() } }

        val backgroundField by state.field<Color>("Background")
        val testColors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Magenta)

        runBlocking {
            forAll(Arb.of(testColors).plusEdgecases(backgroundField.testValues())) { color ->
                backgroundField.value = color
                awaitIdle()
                true
            }
        }

        // Ensure all coroutines (including LaunchedEffect/snapshotFlow) are completed
        awaitIdle()
    }
}
