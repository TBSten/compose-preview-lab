@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class DoubleFieldTest {
    @Test
    fun `DoubleField should update price when value changes`() = runDesktopComposeUiTest {
        forAll(Arb.double().filterNot { it.isNaN() || it.isInfinite() }) { doubleValue ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { DoubleFieldExample() } }

            val priceField = state.field<Double>("Price")
            priceField.value = doubleValue

            awaitIdle()

            onAllNodesWithText("Price: $$doubleValue")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
