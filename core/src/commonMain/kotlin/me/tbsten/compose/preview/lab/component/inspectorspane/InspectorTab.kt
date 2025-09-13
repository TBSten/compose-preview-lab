package me.tbsten.compose.preview.lab.component.inspectorspane

import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.component.EventListSection
import me.tbsten.compose.preview.lab.component.FieldListSection
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_edit
import me.tbsten.compose.preview.lab.core.generated.resources.icon_history
import org.jetbrains.compose.resources.DrawableResource

internal enum class InspectorTab(
    val title: String,
    val iconRes: DrawableResource,
    val content: @Composable (state: PreviewLabState) -> Unit,
) {
    Fields(
        title = "Fields",
        iconRes = Res.drawable.icon_edit,
        content = { state ->
            FieldListSection(
                fields = state.scope.fields,
            )
        },
    ),
    Events(
        title = "Events",
        iconRes = Res.drawable.icon_history,
        content = { state ->
            EventListSection(
                events = state.scope.events,
                selectedEvent = state.selectedEvent,
                onClear = { state.scope.events.clear() },
            )
        },
    ),
}
