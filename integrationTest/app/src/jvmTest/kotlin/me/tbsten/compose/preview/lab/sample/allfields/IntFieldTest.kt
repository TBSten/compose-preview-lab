@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
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
class IntFieldTest : PropertyTestBase() {
    @Test
    fun `IntField should update Counter when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { IntFieldExample() } }

        val countField by state.field<Int>("Count")

        runBlocking {
            forAll(Arb.int().plusEdgecases(countField.testValues())) { intValue ->
                countField.value = intValue
                awaitIdle()

                onNodeWithText("Count: $intValue")
                    .assertExists()
                true
            }
        }

        // Ensure all coroutines (including LaunchedEffect/snapshotFlow) are completed
        awaitIdle()
    }
}
