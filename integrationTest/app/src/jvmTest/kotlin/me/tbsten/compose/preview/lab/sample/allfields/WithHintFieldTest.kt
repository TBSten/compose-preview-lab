@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.forAll
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.sample.PBT
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class WithHintFieldTest : StringSpec({
    "WithHintField should update font size from hints".config(tags = setOf(PBT)) {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { WithHintFieldExample() } }

            val fontSizeField by state.field<TextUnit>("Font Size")

            forAll(Arb.float(8f..32f).map { it.sp }.plusEdgecases(fontSizeField.testValues())) { fontSize ->
                fontSizeField.value = fontSize
                awaitIdle()
                true
            }

            awaitIdle()
        }
    }

    "WithHintField testValues should include hint values" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { WithHintFieldExample() } }

            val fontSizeField by state.field<TextUnit>("Font Size")

            assertSoftly {
                fontSizeField.testValues() shouldContain 12.sp
                fontSizeField.testValues() shouldContain 16.sp
                fontSizeField.testValues() shouldContain 20.sp
                fontSizeField.testValues() shouldContain 24.sp
            }
        }
    }

    "WithHintActionExample testValues should contain initial value only" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { WithHintActionExample() } }

            val itemsField by state.field<List<String>>("Items")

            assertSoftly {
                // ActionChoice should not contribute to testValues
                // Only the initial value (empty list) should be in testValues
                itemsField.testValues() shouldContain emptyList()
                // Action results like listOf("Item A", "Item B", "Item C") should NOT be in testValues
                itemsField.testValues() shouldNotContain listOf("Item A", "Item B", "Item C")
            }
        }
    }
})
