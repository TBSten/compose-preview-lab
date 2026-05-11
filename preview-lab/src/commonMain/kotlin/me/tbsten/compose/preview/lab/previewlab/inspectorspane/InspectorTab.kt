package me.tbsten.compose.preview.lab.previewlab.inspectorspane

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_edit
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_history
import org.jetbrains.compose.resources.painterResource

/**
 * Tab in the PreviewLab inspector panel. Implement this interface for a custom tab; the
 * built-in [Fields], [Events], [KDoc], and [Code] objects below cover the standard cases.
 *
 * Custom tab — implement [Content] with a [ContentContext] receiver to reach
 * [PreviewLabState]:
 *
 * ```kt
 * object CustomTab : InspectorTab {
 *     override val title = "Custom"
 *     override val icon: @Composable () -> Painter = { painterResource(...) }
 *
 *     @Composable
 *     override fun ContentContext.Content() {
 *         Column {
 *             Text("Field count: ${state.scope.fields.size}")
 *         }
 *     }
 * }
 * ```
 *
 * Pass the tab via `PreviewLab(inspectorTabs = InspectorTab.defaults + listOf(CustomTab))`.
 */
interface InspectorTab {
    val title: String

    /** Optional icon; tabs without an icon render as text-only. */
    val icon: (@Composable () -> Painter)? get() = null

    /** Renders tab content. Use the [ContentContext] receiver to access [PreviewLabState]. */
    @Composable
    fun ContentContext.Content()

    /** Receiver passed to [Content]; exposes the live [PreviewLabState] and the tab list. */
    class ContentContext(val state: PreviewLabState, val inspectorTabs: List<InspectorTab>)

    /** Built-in tab listing interactive fields. */
    data object Fields : InspectorTab {
        override val title: String = "Fields"
        override val icon: @Composable () -> Painter = { painterResource(PreviewLabUiRes.drawable.icon_edit) }

        @Composable
        override fun ContentContext.Content() {
            FieldListSection(
                fields = state.fields,
            )
        }
    }

    /** Built-in tab listing events logged via [PreviewLabScope.onEvent]. */
    data object Events : InspectorTab {
        override val title: String = "Events"
        override val icon: @Composable () -> Painter = { painterResource(PreviewLabUiRes.drawable.icon_history) }

        @Composable
        override fun ContentContext.Content() {
            EventListSection(
                events = state.events,
                selectedEvent = state.selectedEvent,
                onClear = { state.events.clear() },
            )
        }
    }

    /** Built-in tab showing the preview function's KDoc, or `(No KDoc)` if none. */
    data object KDoc : InspectorTab {
        override val title: String = "KDoc"

        @Composable
        override fun ContentContext.Content() {
            val kdoc = LocalPreviewLabPreview.current?.kdoc

            SelectionContainer {
                PreviewLabText(
                    text = kdoc ?: "(No KDoc)",
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                        .fillMaxSize(),
                )
            }
        }
    }

    @ExperimentalComposePreviewLabApi
    data object Code : InspectorTab {
        override val title: String = "Code"

        @Composable
        override fun ContentContext.Content() {
            val code = LocalPreviewLabPreview.current?.code

            SelectionContainer {
                PreviewLabText(
                    text = if (code != null) {
                        state.fields.fold(code) { acc, field ->
                            val valueCode = field.valueCode()
                            val escapedLabel = Regex.escape(field.label)
                            acc.replace(
                                Regex("""fieldValue\s*\{[^}]*?"$escapedLabel"[^}]*?}"""),
                            ) {
                                valueCode
                            }
                        }
                    } else {
                        "No code"
                    },
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                        .fillMaxSize(),
                )
            }
        }
    }

    companion object {
        val defaults: List<InspectorTab> = listOf(Fields, Events, KDoc)
    }
}
