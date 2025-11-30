@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class WithHintFieldTest {
    @Test
    fun `WithHintField should update font size from hints`() = runDesktopComposeUiTest {
        checkAll(listOf(12.sp, 16.sp, 20.sp, 24.sp).exhaustive()) { fontSize ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { WithHintFieldExample() } }

            val fontSizeField = state.field<TextUnit>("Font Size")
            fontSizeField.value = fontSize

            awaitIdle()
        }
    }
}
