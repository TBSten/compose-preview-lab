@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
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
class EnumFieldTest {
    @Test
    fun `EnumField should update button variant when selection changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { EnumFieldExample() } }

        val variantField = state.field<ButtonVariant>("Variant")

        forAll(Arb.of(variantField.testValues()).plusEdgecases(variantField.testValues())) { variant ->
            variantField.value = variant
            awaitIdle()

            onAllNodesWithText(variant.name)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
