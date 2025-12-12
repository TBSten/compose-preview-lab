@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class DoubleFieldTest {
    @Test
    fun `DoubleField should update price when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { DoubleFieldExample() } }

        val priceField by state.field<Double>("Price")

        forAll(Arb.double().filterNot { it.isNaN() || it.isInfinite() }.plusEdgecases(priceField.testValues())) { doubleValue ->
            priceField.value = doubleValue
            awaitIdle()

            onAllNodesWithText("Price: $$doubleValue")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
