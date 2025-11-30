@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class SelectableFieldTest {
    @Test
    fun `SelectableField should update theme when selection changes`() = runDesktopComposeUiTest {
        checkAll(listOf("Light", "Dark", "Auto").exhaustive()) { theme ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { SelectableFieldExample() } }

            val themeField = state.field<String>("Theme")
            themeField.value = theme

            awaitIdle()

            onNodeWithText("current theme: $theme")
                .assertExists()
        }
    }
}
