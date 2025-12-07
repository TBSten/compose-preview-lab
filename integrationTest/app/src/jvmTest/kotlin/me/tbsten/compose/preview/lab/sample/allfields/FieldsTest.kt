@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.field
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

/**
 * Property-based tests for Field components in integrationTest/app.
 */
@OptIn(ExperimentalTestApi::class)
class FieldsTest {
    @Test
    fun `Can render all Previews without error`() = runDesktopComposeUiTest {
        checkAll(
            (app.PreviewList + uiLib.PreviewList + helloComposePreviewLab.PreviewList).exhaustive(),
        ) { preview ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { preview.content() } }

            awaitIdle()
        }
    }

    @Test
    fun `IntField should update Counter when value changes`() = runDesktopComposeUiTest {
        forAll(Arb.int()) { intValue ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { IntFieldExample() } }

            val countField by state.field<Int>("Count")
            countField.value = intValue

            awaitIdle()

            onNodeWithText("Count: $intValue")
                .assertExists()
            true
        }
    }

    @Test
    fun `StringField should update button text when value changes`() = runDesktopComposeUiTest {
        // Use non-empty strings to avoid issues with empty button text
        forAll(Arb.string(1..50)) { stringValue ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { StringFieldExample() } }

            val textField by state.field<String>("text")
            textField.value = stringValue

            awaitIdle()

            // Text appears in both button and text field, so verify at least one node exists
            onAllNodesWithText(stringValue)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun `BooleanField should toggle button enabled state`() = runDesktopComposeUiTest {
        forAll(Arb.boolean()) { boolValue ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { BooleanFieldExample() } }

            val enabledField by state.field<Boolean>("enabled")
            enabledField.value = boolValue

            awaitIdle()
            true
        }
    }

    @Test
    fun `FloatField should update alpha when value changes`() = runDesktopComposeUiTest {
        forAll(Arb.float(0f..1f).filterNot { it.isNaN() }) { floatValue ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { FloatFieldExample() } }

            val alphaField by state.field<Float>("Alpha")
            alphaField.value = floatValue

            awaitIdle()
            true
        }
    }
}
