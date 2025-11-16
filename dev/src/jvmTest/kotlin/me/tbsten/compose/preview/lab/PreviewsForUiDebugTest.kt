package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlin.test.Test

/**
 * Tests for PreviewsForUiDebug previews.
 * These tests verify that PreviewLab works correctly with various field types and UI interactions.
 */
@OptIn(ExperimentalTestApi::class)
class PreviewsForUiDebugTest {
    private val testViewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }
    private val testLifecycleOwner = object : LifecycleOwner {
        private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle by ::lifecycleRegistry
    }

    /**
     * Sets up content with PreviewLab test environment including ViewModelStore and Lifecycle.
     * Returns PreviewLabState for accessing fields and other state.
     */
    private fun DesktopComposeUiTest.setContentInTestEnvironment(content: @Composable () -> Unit): PreviewLabState {
        val state = PreviewLabState()
        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides testViewModelStoreOwner,
                LocalLifecycleOwner provides testLifecycleOwner,
            ) {
                state.Provider {
                    content()
                }
            }
        }
        return state
    }

    @Test
    fun `Fields preview should render`() = runDesktopComposeUiTest {
        setContentInTestEnvironment { PreviewsForUiDebug.Fields.content() }
        // Verify that Fields preview renders without errors
        awaitIdle()
        // Fields preview contains various field types, just verify it renders
    }

    @Test
    fun `IntField should update preview when value changes`() = runDesktopComposeUiTest {
        val state = setContentInTestEnvironment { PreviewsForUiDebug.Fields.content() }

        // Get the IntField
        val intField = state.requireField<Int>("intValue")

        // Change the value
        intField.value = 42

        awaitIdle()

        // Verify the preview updated
        onNodeWithText("intValue: 42")
            .assertIsDisplayed()
    }

    @Test
    fun `StringField should update preview when value changes`() = runDesktopComposeUiTest {
        val state = setContentInTestEnvironment { PreviewsForUiDebug.Fields.content() }

        // Get the StringField
        val stringField = state.requireField<String>("stringValue")

        // Change the value
        stringField.value = "Hello World"

        awaitIdle()

        // Verify the preview updated (note: the label in PreviewsForDebug is "intValue" not "stringValue")
        onNodeWithText("intValue: Hello World")
            .assertIsDisplayed()
    }

    @Test
    fun `BooleanField should toggle preview when value changes`() = runDesktopComposeUiTest {
        val state = setContentInTestEnvironment { PreviewsForUiDebug.Fields.content() }

        // Get the BooleanField
        val boolField = state.requireField<Boolean>("booleanValue")

        // Verify initial state (false)
        onNodeWithText("booleanValue: false")
            .assertExists()

        // Change the value to true
        boolField.value = true

        awaitIdle()

        // Verify the preview updated to true
        onNodeWithText("booleanValue: true")
            .assertExists()

        // Toggle back to false
        boolField.value = false

        awaitIdle()

        // Verify the preview updated back to false
        onNodeWithText("booleanValue: false")
            .assertExists()
    }

    @Test
    fun `FloatField should update preview when value changes`() = runDesktopComposeUiTest {
        val state = setContentInTestEnvironment { PreviewsForUiDebug.Fields.content() }

        // Get the FloatField
        val floatField = state.requireField<Float>("floatField")

        // Change the value
        floatField.value = 3.14f

        awaitIdle()

        // Verify the preview updated
        onNodeWithText("floatField: 3.14")
            .assertIsDisplayed()
    }

    @Test
    fun `Events preview should show toast after call onEvent()`() = runDesktopComposeUiTest {
        setContentInTestEnvironment { PreviewsForUiDebug.Events.content() }

        onNodeWithTag("item:0")
            .performClick()

        awaitIdle()

        onNodeWithTag("Click item 0")
            .assertExists()
    }

    @Test
    fun `Layouts preview should render and respond to clicks`() = runDesktopComposeUiTest {
        setContentInTestEnvironment { PreviewsForUiDebug.Layouts.content() }

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
        setContentInTestEnvironment { PreviewsForUiDebug.ScreenSize.content() }

        awaitIdle()
        // ScreenSize preview uses PreviewLab with AllPresets, verify it renders
    }

    @Test
    fun `WithoutPreviewLab should render simple text`() = runDesktopComposeUiTest {
        setContentInTestEnvironment { PreviewsForUiDebug.WithoutPreviewLab.content() }

        onNodeWithText("Without PreviewLab { }")
            .assertIsDisplayed()
    }

    @Test
    fun `ButtonPrimary should render and trigger event on click`() = runDesktopComposeUiTest {
        setContentInTestEnvironment { PreviewsForUiDebug.ButtonPrimary.content() }

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
        setContentInTestEnvironment { PreviewsForUiDebug.ButtonSecondary.content() }

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
        setContentInTestEnvironment { PreviewsForUiDebug.HeadingText.content() }

        awaitIdle()

        onNodeWithText("Heading Text")
            .assertIsDisplayed()
    }

    @Test
    fun `LoginForm should render`() = runDesktopComposeUiTest {
        setContentInTestEnvironment { PreviewsForUiDebug.LoginForm.content() }

        awaitIdle()

        onNodeWithText("Login Form Preview")
            .assertIsDisplayed()
    }

    @Test
    fun `ProfileSettings should render`() = runDesktopComposeUiTest {
        setContentInTestEnvironment { PreviewsForUiDebug.ProfileSettings.content() }

        awaitIdle()

        onNodeWithText("Profile Settings Preview")
            .assertIsDisplayed()
    }

    @Test
    fun `ModifierAndComposableField should render button with fields`() = runDesktopComposeUiTest {
        setContentInTestEnvironment { PreviewsForUiDebug.ModifierAndComposableField.content() }

        awaitIdle()

        // ModifierAndComposableField contains a Button with ComposableField
        // Just verify it renders without errors
    }
}
