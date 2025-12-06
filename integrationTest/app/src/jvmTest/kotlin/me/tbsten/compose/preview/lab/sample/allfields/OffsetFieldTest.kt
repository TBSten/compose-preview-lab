@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
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
class OffsetFieldTest {
    @Test
    fun `OffsetField should update position when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { OffsetFieldExample() } }

        val positionField = state.field<Offset>("Position")

        forAll(Arb.float(-200f..200f).map { Offset(it, it) }.plusEdgecases(positionField.testValues())) { offset ->
            positionField.value = offset
            awaitIdle()
            true
        }
    }
}
