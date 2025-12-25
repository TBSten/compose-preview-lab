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
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class PreviewsForUiDebugTest : StringSpec({

    "Fields preview should render" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }
            awaitIdle()
        }
    }

    "IntField should update preview when value changes" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }

            val intField by state.field<Int>("intValue")

            forAll(Arb.int().plusEdgecases(intField.testValues())) { intFieldValue ->
                intField.value = intFieldValue
                awaitIdle()

                onNodeWithText("intValue: $intFieldValue")
                    .isDisplayed()
            }
        }
    }

    "StringField should update preview when value changes" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }
            val stringField by state.field<String>("stringValue")

            forAll(Arb.string().plusEdgecases(stringField.testValues())) { stringFieldValue ->
                stringField.value = stringFieldValue
                awaitIdle()

                onNodeWithText("stringValue: $stringFieldValue")
                    .isDisplayed()
            }
        }
    }

    "BooleanField should toggle preview when value changes" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }
            val boolField by state.field<Boolean>("booleanValue")

            forAll(Arb.boolean().plusEdgecases(boolField.testValues())) { boolFieldValue ->
                boolField.value = boolFieldValue
                awaitIdle()

                onNodeWithText("booleanValue: $boolFieldValue")
                    .isDisplayed()
            }
        }
    }

    "FloatField should update preview when value changes" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.Fields.content() } }
            val floatField by state.field<Float>("floatField")

            forAll(Arb.float().plusEdgecases(floatField.testValues())) { floatFieldValue ->
                floatField.value = floatFieldValue
                awaitIdle()

                onAllNodesWithText("floatField: $floatFieldValue")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
        }
    }

    "Events preview should render and respond to clicks" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.Events.content() } }

            awaitIdle()

            onNodeWithTag("item:0")
                .assertExists()
                .performClick()

            awaitIdle()

            onAllNodesWithText("Click item 0")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    "Layouts preview should render and respond to clicks" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.Layouts.content() } }

            awaitIdle()

            onNodeWithText("Layouts")
                .assertIsDisplayed()

            onNodeWithTag("item:0")
                .assertExists()
                .performClick()

            awaitIdle()
        }
    }

    "ScreenSize preview should render with multiple screen sizes" {
        runDesktopComposeUiTest(
            width = 1920,
            height = 1080,
        ) {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.ScreenSize.content() } }

            awaitIdle()
        }
    }

    "WithoutPreviewLab should render simple text" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.WithoutPreviewLab.content() } }

            onNodeWithText("Without PreviewLab { }")
                .assertIsDisplayed()
        }
    }

    "ButtonPrimary should render and trigger event on click" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.ButtonPrimary.content() } }

            awaitIdle()

            onNodeWithTag("PrimaryButton", useUnmergedTree = true)
                .assertIsDisplayed()
                .performClick()

            awaitIdle()

            state.events.any { it.title == "onClick" } shouldBe true
        }
    }

    "ButtonSecondary should render and trigger event on click" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.ButtonSecondary.content() } }

            awaitIdle()

            onNodeWithTag("SecondaryButton", useUnmergedTree = true)
                .assertIsDisplayed()
                .performClick()

            awaitIdle()

            state.events.any { it.title == "SecondaryButton.onClick" } shouldBe true
        }
    }

    "HeadingText should render with correct style" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.HeadingText.content() } }

            awaitIdle()

            onNodeWithText("Heading Text")
                .assertIsDisplayed()
        }
    }

    "LoginForm should render" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.LoginForm.content() } }

            awaitIdle()

            onNodeWithText("Login Form Preview")
                .assertIsDisplayed()
        }
    }

    "ProfileSettings should render" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.ProfileSettings.content() } }

            awaitIdle()

            onNodeWithText("Profile Settings Preview")
                .assertIsDisplayed()
        }
    }

    "ModifierAndComposableField should render button with fields" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { PreviewsForUiDebug.ModifierAndComposableField.content() } }

            awaitIdle()
        }
    }
})
