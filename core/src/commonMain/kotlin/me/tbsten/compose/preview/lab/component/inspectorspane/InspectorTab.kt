package me.tbsten.compose.preview.lab.component.inspectorspane

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.component.EventListSection
import me.tbsten.compose.preview.lab.component.FieldListSection
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_edit
import me.tbsten.compose.preview.lab.core.generated.resources.icon_history
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
 * object CustomTab(
 *     override val title: String = "Custom",
 *     override val icon: @Composable () -> Painter = { painterResource(Res.drawable.icon_custom) },
 *     override val content: @Composable (state: PreviewLabState) -> Unit = { state ->
 *         // Your custom tab content
 *         Column {
 *             Text("Custom Tab Content")
 *         }
 *     }
 * ) : InspectorTab
 * ```
 *
 * Then pass it to PreviewLab via the `additionalTabs` parameter:
 *
 * ```kt
 * @Preview
 * @Composable
 * fun MyPreview() = PreviewLab(
 *     additionalTabs = listOf(CustomTab())
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
     * The icon to display for the tab
     */
    val icon: @Composable () -> Painter

    /**
     * The content to display when the tab is selected
     */
    val content: @Composable (state: PreviewLabState) -> Unit

    /**
     * Built-in Fields tab that displays all interactive fields.
     */
    data object Fields : InspectorTab {
        override val title: String = "Fields"
        override val icon: @Composable () -> Painter = { painterResource(Res.drawable.icon_edit) }
        override val content: @Composable (state: PreviewLabState) -> Unit = { state ->
            FieldListSection(
                fields = state.scope.fields,
            )
        }
    }

    /**
     * Built-in Events tab that displays all logged events.
     */
    data object Events : InspectorTab {
        override val title: String = "Events"
        override val icon: @Composable () -> Painter = { painterResource(Res.drawable.icon_history) }
        override val content: @Composable (state: PreviewLabState) -> Unit = { state ->
            EventListSection(
                events = state.scope.events,
                selectedEvent = state.selectedEvent,
                onClear = { state.scope.events.clear() },
            )
        }
    }

    companion object {
        /**
         * Default built-in tabs: Fields and Events
         */
        val defaults: List<InspectorTab> = listOf(Fields, Events)
    }
}
