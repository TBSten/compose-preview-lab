package me.tbsten.compose.preview.lab.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test

/**
 * Simple integration tests for the integrationTest module.
 * These tests verify basic Compose UI functionality.
 */
@OptIn(ExperimentalTestApi::class)
class SimpleIntegrationTest {

    @Test
    fun `simple text should render`() = runDesktopComposeUiTest {
        setContent {
            MaterialTheme {
                Text("Integration Test")
            }
        }

        onNodeWithText("Integration Test").assertIsDisplayed()
    }

    @Test
    fun `button with state should work`() = runDesktopComposeUiTest {
        setContent {
            MaterialTheme {
                var count by remember { mutableStateOf(0) }

                Column {
                    Text("Count: $count")
                    Button(onClick = { count++ }) {
                        Text("Increment")
                    }
                }
            }
        }

        onNodeWithText("Count: 0").assertIsDisplayed()
        onNodeWithText("Increment").performClick()
        onNodeWithText("Count: 1").assertIsDisplayed()
    }

    @Test
    fun `multiple clicks should update state`() = runDesktopComposeUiTest {
        setContent {
            MaterialTheme {
                var clicks by remember { mutableStateOf(0) }

                Button(onClick = { clicks++ }) {
                    Text("Clicked $clicks times")
                }
            }
        }

        onNodeWithText("Clicked 0 times").assertIsDisplayed()
        onNodeWithText("Clicked 0 times").performClick()
        onNodeWithText("Clicked 1 times").assertIsDisplayed()
        onNodeWithText("Clicked 1 times").performClick()
        onNodeWithText("Clicked 2 times").assertIsDisplayed()
    }
}
