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
 * Represents a tab in the PreviewLab inspector panel.
 *
 * Built-in tabs (Fields and Events) are provided by [InspectorTab.Fields] and [InspectorTab.Events].
 * Custom tabs can be created by implementing this interface.
 *
 * ## Creating Custom Tabs
 *
 * To create a custom tab, implement this interface with your own tab content:
 *
 * ```kt
 * object CustomTab : InspectorTab {
 *     override val title = "Custom"
 *     override val icon: @Composable () -> Painter = { painterResource(PreviewLabUiRes.drawable.icon_custom) }
 *
 *     @Composable
 *     override fun ContentContext.Content() {
 *         // Your custom tab content can access state via the ContentContext
 *         Column {
 *             Text("Custom Tab Content")
 *             Text("Field count: ${state.scope.fields.size}")
 *         }
 *     }
 * }
 * ```
 *
 * Then pass it to PreviewLab via the `inspectorTabs` parameter:
 *
 * ```kt
 * // Default tabs (Fields, Events) + custom tab
 * @Preview
 * @Composable
 * fun MyPreview() = PreviewLab(
 *     inspectorTabs = InspectorTab.defaults + listOf(CustomTab)
 * ) {
 *     MyComponent()
 * }
 *
 * // Custom tabs only (no default tabs)
 * @Preview
 * @Composable
 * fun MyPreview() = PreviewLab(
 *     inspectorTabs = listOf(CustomTab)
 * ) {
 *     MyComponent()
 * }
 * ```
 */
interface InspectorTab {
    /**
     * The display title of the tab
     */
    val title: String

    /**
     * The icon to display for the tab.
     * If null, the tab will be displayed without an icon.
     */
    val icon: (@Composable () -> Painter)? get() = null

    /**
     * The content to display when the tab is selected.
     * Implement this composable function within [ContentContext] receiver scope to access
     * the PreviewLabState via [ContentContext.state].
     */
    @Composable
    fun ContentContext.Content()

    /**
     * Context providing access to [PreviewLabState] for tab content.
     *
     * @property state The current [PreviewLabState], providing access to fields, events, and other preview state
     */
    class ContentContext(val state: PreviewLabState, val inspectorTabs: List<InspectorTab>)

    /**
     * Built-in Fields tab that displays all interactive fields.
     *
     * @see InspectorTab
     */
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

    /**
     * Built-in Events tab that displays all logged events.
     *
     * @see InspectorTab
     */
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

    data object KDoc : InspectorTab {
        override val title: String = "Code"

        @Composable
        override fun ContentContext.Content() {
            val kdoc = LocalPreviewLabPreview.current?.kdoc

            SelectionContainer {
                PreviewLabText(
                    text = kdoc ?: "(No code KDoc)",
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
        /**
         * Default built-in tabs: Fields and Events
         */
        val defaults: List<InspectorTab> = listOf(Fields, Events, KDoc)
    }
}
