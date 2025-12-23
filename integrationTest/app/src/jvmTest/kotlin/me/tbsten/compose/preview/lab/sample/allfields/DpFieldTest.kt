@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.kotest.property.Arb
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.map
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
class DpFieldTest : PropertyTestBase() {
    @Test
    fun `DpField should update padding when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { DpFieldExample() } }

        val paddingField by state.field<Dp>("Padding")

        runBlocking {
            forAll(Arb.float(0f..100f).map { it.dp }.plusEdgecases(paddingField.testValues())) { dpValue ->
                paddingField.value = dpValue
                awaitIdle()
                true
            }
        }

        // Ensure all coroutines (including LaunchedEffect/snapshotFlow) are completed
        awaitIdle()
    }
}
