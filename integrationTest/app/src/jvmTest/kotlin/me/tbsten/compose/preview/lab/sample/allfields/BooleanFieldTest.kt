@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.testing.TestPreviewLab
import me.tbsten.compose.preview.lab.testing.field

@OptIn(ExperimentalTestApi::class)
class BooleanFieldTest {
    @Test
    fun `BooleanField should toggle button enabled state`() = runDesktopComposeUiTest {
        forAll(Arb.boolean()) { boolValue ->
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { BooleanFieldExample() } }

            val enabledField = state.field<Boolean>("enabled")
            enabledField.value = boolValue

            awaitIdle()
            true
        }
    }
}
