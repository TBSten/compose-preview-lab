@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.field

@OptIn(ExperimentalTestApi::class)
class CombinedFieldTest {
    @Test
    fun `CombinedField should update padding when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { CombinedFieldExample() } }

        val paddingField = state.field<CombinedFieldPadding>("Padding")

        forAll(Arb.of(paddingField.testValues()).plusEdgecases(paddingField.testValues())) { padding ->
            paddingField.value = padding
            awaitIdle()
            true
        }
    }
}
