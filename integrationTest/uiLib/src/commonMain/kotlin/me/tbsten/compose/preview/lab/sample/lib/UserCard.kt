@file:OptIn(me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.sample.lib

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.previewlab.autoEvent
import me.tbsten.compose.preview.lab.previewlab.autoField

/**
 * A small showcase component used by the autoField / autoEvent integration preview.
 *
 * Has a mix of String / Int / Boolean primitives plus two `() -> Unit` callbacks so the
 * preview can exercise every shape the compiler plugin should rewrite.
 */
@Composable
fun UserCard(
    name: String,
    age: Int,
    isPremium: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.padding(8.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (isPremium) "★ $name" else name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "age $age",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onClick) { Text("Click") }
                TextButton(onClick = onLongClick) { Text("Long Click") }
            }
        }
    }
}

/**
 * Showcase Preview for `autoField()` / `autoEvent()` (issue #69 prototype).
 *
 * Every parameter of [UserCard] is wired through one of the auto-helpers — the compiler
 * plugin injects the parameter name as the field/event label, so the inspector pane
 * automatically renders one field per primitive parameter labelled "name", "age",
 * "isPremium" and event toasts labelled "onClick", "onLongClick".
 */
@ComposePreviewLabOption(
    id = "UserCardAutoFieldEventPreview",
    displayName = "autoField / autoEvent (issue #69)",
)
@Preview
@Composable
private fun UserCardAutoFieldEventPreview() = PreviewLab {
    UserCard(
        name = autoField(),
        age = autoField(),
        isPremium = autoField(),
        onClick = autoEvent(),
        onLongClick = autoEvent(),
    )
}
