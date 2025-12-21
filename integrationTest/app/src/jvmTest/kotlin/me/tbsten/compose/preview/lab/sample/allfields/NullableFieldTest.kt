@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import kotlin.test.Test
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.sample.PropertyTestBase
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class NullableFieldTest : PropertyTestBase() {
    @Test
    fun `NullableField should update user name when value changes`() = runDesktopComposeUiTest {
        val state = PreviewLabState()
        setContent { TestPreviewLab(state) { NullableFieldExample() } }

        val userNameField by state.field<String?>("User Name")

        forAll(Arb.string(1..20).orNull().plusEdgecases(userNameField.testValues())) { userName ->
            userNameField.value = userName
            awaitIdle()

            val expectedText = userName ?: "No user name"
            onAllNodesWithText(expectedText)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }
}
