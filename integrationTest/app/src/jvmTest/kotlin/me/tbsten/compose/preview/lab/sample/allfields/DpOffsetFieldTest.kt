@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
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
class DpOffsetFieldTest {
    @Test
    fun `DpOffsetField should update offset when value changes`() = runDesktopComposeUiTest {
        forAll(
            Arb.float(-50f..50f).map { DpOffset(it.dp, it.dp) }
        ) { offset ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { DpOffsetFieldExample() } }

            val offsetField = state.field<DpOffset>("Offset")
            offsetField.value = offset

            awaitIdle()
            true
        }
    }
}
