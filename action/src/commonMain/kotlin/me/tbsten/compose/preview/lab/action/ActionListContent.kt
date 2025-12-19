package me.tbsten.compose.preview.lab.action

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabAction
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * Scope for defining actions in the action list.
 */
@ExperimentalComposePreviewLabApi
class ActionListContentScope

/**
 * Displays a list of actions in the Preview Lab UI.
 *
 * @param actions The list of actions to display
 * @param onActionExecute Callback invoked when an action is executed
 * @param modifier The modifier to apply to the action list
 */
@ExperimentalComposePreviewLabApi
@Composable
fun ActionListContent(
    actions: List<PreviewLabAction<*>>,
    onActionExecute: (PreviewLabAction<*>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (actions.isEmpty()) {
            Text(
                text = "No actions registered",
                style = PreviewLabTheme.typography.body1,
            )
        } else {
            actions.forEach { action ->
                ActionItem(
                    action = action,
                    onExecute = { onActionExecute(action) },
                )
            }
        }
    }
}

@ExperimentalComposePreviewLabApi
@Composable
private fun ActionItem(
    action: PreviewLabAction<*>,
    onExecute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO: Implement action item UI with configurable fields and execute button
    Text(
        text = action.label,
        style = PreviewLabTheme.typography.label1,
        modifier = modifier,
    )
}
