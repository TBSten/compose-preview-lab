@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.plusEdgecases
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.field
import me.tbsten.compose.preview.lab.sample.PBT
import me.tbsten.compose.preview.lab.testing.TestPreviewLab

@OptIn(ExperimentalTestApi::class)
class SetFieldTest : StringSpec({
    tags(PBT)

    "SetField should update text when set value changes" {
        runDesktopComposeUiTest {
            val state = PreviewLabState()
            setContent { TestPreviewLab(state) { SetFieldExample() } }

            val fruitsField by state.field<Set<String>>("fruits")

            forAll(
                Arb.set(Arb.string(1..20), 0..10)
                    .plusEdgecases(fruitsField.testValues()),
            ) { setValue ->
                fruitsField.value = setValue
                awaitIdle()
                true
            }

            awaitIdle()
        }
    }
})
