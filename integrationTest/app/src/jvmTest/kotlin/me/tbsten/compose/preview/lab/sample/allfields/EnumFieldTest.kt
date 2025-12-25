@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.sample.PBT
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class EnumFieldTest : StringSpec({
    tags(PBT)

    "EnumField should update button variant when selection changes" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { EnumFieldExample() } }

            val variantField by state.field<ButtonVariant>("Variant")

            forAll(Arb.of(variantField.testValues()).plusEdgecases(variantField.testValues())) { variant ->
                variantField.value = variant
                awaitIdle()

                onAllNodesWithText(variant.name)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }

            awaitIdle()
        }
    }
})
