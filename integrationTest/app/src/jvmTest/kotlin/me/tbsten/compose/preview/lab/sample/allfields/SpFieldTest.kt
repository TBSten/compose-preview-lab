@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.kotest.property.Arb
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class SpFieldTest {
    @Test
    fun `SpField should update font size when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { SpFieldExample() } }

        val fontSizeField = state.field<TextUnit>("Font Size")

        forAll(Arb.float(8f..48f).map { it.sp }.plusEdgecases(fontSizeField.testValues())) { spValue ->
            fontSizeField.value = spValue
            awaitIdle()
            true
        }
    }
}
