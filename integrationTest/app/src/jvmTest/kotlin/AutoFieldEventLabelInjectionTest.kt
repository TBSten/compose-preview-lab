@file:OptIn(
    androidx.compose.ui.test.ExperimentalTestApi::class,
    me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi::class,
)

package app

import androidx.compose.ui.test.runComposeUiTest
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewlab.autoEvent
import me.tbsten.compose.preview.lab.previewlab.autoField
import me.tbsten.compose.preview.lab.sample.lib.UserCard

/**
 * End-to-end check that the `autoField()` / `autoEvent()` IR transform injects the
 * surrounding parameter name as the field/event label.
 *
 * `compiler-plugin` already has a unit test ([AutoFieldEventLabelInjectionTest][me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.AutoFieldEventLabelInjectionTest])
 * exercising the IR pass against stub runtime symbols. This integration test runs the
 * real preview-lab runtime (with the real `PreviewLabScope.autoField` reified dispatch)
 * inside a `runComposeUiTest`, so any breakage between the IR pass and the runtime
 * surfaces here.
 */
class AutoFieldEventLabelInjectionTest :
    FunSpec({
        test("autoField / autoEvent inside PreviewLab pick up the parameter names as labels") {
            val state = PreviewLabState()

            runComposeUiTest {
                setContent {
                    PreviewLab(state = state) {
                        UserCard(
                            name = autoField(),
                            age = autoField(),
                            isPremium = autoField(),
                            onClick = autoEvent(),
                            onLongClick = autoEvent(),
                        )
                    }
                }
                waitForIdle()

                // Field assertions must happen *inside* the test block — `fieldValue { ... }`
                // registers each field via a `DisposableEffect` whose `onDispose` removes the
                // entry, so the list is empty once the composition is torn down at the end
                // of `runComposeUiTest`.
                // `PreviewLab` registers its own `ScreenSize` field at the top of the list,
                // so we drop the built-in entries before asserting on the labels generated
                // by the `autoField()` plugin transform.
                val userFieldLabels = state.fields
                    .map { it.label }
                    .filter { it != "ScreenSize" }
                userFieldLabels shouldContainExactly listOf("name", "age", "isPremium")

                // The event lambdas are not invoked by the gallery render, so no events
                // are recorded yet — verifying this catches a regression where `autoEvent`
                // accidentally fires its `onEvent` at composition time instead of inside
                // the returned lambda.
                assertSoftly {
                    state.events.size shouldBe 0
                }
            }
        }
    })
