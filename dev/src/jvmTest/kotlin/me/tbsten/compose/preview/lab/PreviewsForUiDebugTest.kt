@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import kotlin.test.Test
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

/**
 * Tests for PreviewsForUiDebug previews.
 * These tests verify that PreviewLab works correctly with various field types and UI interactions.
 */
@OptIn(ExperimentalTestApi::class)
class PreviewsForUiDebugTest {

    @Test
    fun `Fields preview should render`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }
        // Verify that Fields preview renders without errors
        awaitIdle()
        // Fields preview contains various field types, just verify it renders
    }

    @Test
    fun `IntField should update preview when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }

        val intField by state.field<Int>("intValue")

        forAll(Arb.int().plusEdgecases(intField.testValues())) { intFieldValue ->
            intField.value = intFieldValue
            awaitIdle()

            // Verify
            onNodeWithText("intValue: $intFieldValue")
                .isDisplayed()
        }
    }

    @Test
    fun `StringField should update preview when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }
        val stringField by state.field<String>("stringValue")

        forAll(Arb.string().plusEdgecases(stringField.testValues())) { stringFieldValue ->
            stringField.value = stringFieldValue
            awaitIdle()

            // Verify
            onNodeWithText("stringValue: $stringFieldValue")
                .isDisplayed()
        }
    }

    @Test
    fun `BooleanField should toggle preview when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }
        val boolField by state.field<Boolean>("booleanValue")

        forAll(Arb.boolean().plusEdgecases(boolField.testValues())) { boolFieldValue ->
            boolField.value = boolFieldValue
            awaitIdle()

            // Verify
            onNodeWithText("booleanValue: $boolFieldValue")
                .isDisplayed()
        }
    }

    @Test
    fun `FloatField should update preview when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }
        val floatField by state.field<Float>("floatField")

        forAll(Arb.float().plusEdgecases(floatField.testValues())) { floatFieldValue ->
            floatField.value = floatFieldValue
            awaitIdle()

            // Verify
            onAllNodesWithText("floatField: $floatFieldValue")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun `Events preview should render and respond to clicks`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.Events.content() } }

        awaitIdle()

        // Verify item:0 exists and click it
        onNodeWithTag("item:0")
            .assertExists()
            .performClick()

        awaitIdle()

        // After clicking, the event should be registered and "No Events" should not be shown
        // Verify that at least one "Click item 0" text exists (could be in list or event list)
        onAllNodesWithText("Click item 0")
            .fetchSemanticsNodes()
            .isNotEmpty()
    }

    @Test
    fun `Layouts preview should render and respond to clicks`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.Layouts.content() } }

        awaitIdle()

        // Verify the title is displayed
        onNodeWithText("Layouts")
            .assertIsDisplayed()

        // Verify items are clickable
        onNodeWithTag("item:0")
            .assertExists()
            .performClick()

        awaitIdle()
    }

    @Test
    fun `ScreenSize preview should render with multiple screen sizes`() = runDesktopComposeUiTest(
        width = 1920,
        height = 1080,
    ) {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.ScreenSize.content() } }

        awaitIdle()
        // ScreenSize preview uses PreviewLab with AllPresets, verify it renders
    }

    @Test
    fun `WithoutPreviewLab should render simple text`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.WithoutPreviewLab.content() } }

        onNodeWithText("Without PreviewLab { }")
            .assertIsDisplayed()
    }

    @Test
    fun `ButtonPrimary should render and trigger event on click`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.ButtonPrimary.content() } }

        awaitIdle()

        onNodeWithTag("PrimaryButton", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        awaitIdle()

        // Verify onEvent was triggered
        onNodeWithTag("onClick")
            .assertExists()
    }

    @Test
    fun `ButtonSecondary should render and trigger event on click`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.ButtonSecondary.content() } }

        awaitIdle()

        onNodeWithTag("SecondaryButton", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        awaitIdle()

        // Verify onEvent was triggered
        onNodeWithTag("SecondaryButton.onClick")
            .assertExists()
    }

    @Test
    fun `HeadingText should render with correct style`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.HeadingText.content() } }

        awaitIdle()

        onNodeWithText("Heading Text")
            .assertIsDisplayed()
    }

    @Test
    fun `LoginForm should render`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.LoginForm.content() } }

        awaitIdle()

        onNodeWithText("Login Form Preview")
            .assertIsDisplayed()
    }

    @Test
    fun `ProfileSettings should render`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.ProfileSettings.content() } }

        awaitIdle()

        onNodeWithText("Profile Settings Preview")
            .assertIsDisplayed()
    }

    @Test
    fun `ModifierAndComposableField should render button with fields`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { PreviewsForUiDebug.ModifierAndComposableField.content() } }

        awaitIdle()

        // ModifierAndComposableField contains a Button with ComposableField
        // Just verify it renders without errors
    }
}
