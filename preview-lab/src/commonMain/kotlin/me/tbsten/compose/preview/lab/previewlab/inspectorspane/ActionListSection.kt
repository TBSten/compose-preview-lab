package me.tbsten.compose.preview.lab.previewlab.inspectorspane

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.tbsten.compose.preview.lab.PreviewLabAction
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.Divider

@Composable
internal fun ActionListSection(actions: List<PreviewLabAction<*>>) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
        ) {
            actions.forEachIndexed { index, action ->
                ActionListItem(action = action)

                if (index != actions.lastIndex) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun ActionListItem(action: PreviewLabAction<*>) {
    // TODO もう少し広い CoroutineScope にしないと行けなさそう
    val coroutineScope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            text = "${action.label}(${action.argFields.joinToString(", ") { "${it.label} = ..." }})",
            onClick = {
                coroutineScope.launch {
                    action.action()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (action.argFields.isNotEmpty()) {
            Row(Modifier.height(IntrinsicSize.Min)) {
                Divider(modifier = Modifier.padding(start = 4.dp, end = 8.dp).fillMaxHeight())

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    action.argFields.forEach { argField ->
                        argField.View()
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
