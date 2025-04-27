package me.tbsten.compose.preview.lab.me

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreviewLab(
    content: @Composable PreviewLabScope.() -> Unit,
) {
    val scope = remember { PreviewLabScope() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // TODO content, info, switch
            content(scope)
        }

        Column(
            modifier = Modifier
                .widthIn(min = 150.dp, max = 300.dp)
                .fillMaxHeight()
        ) {
            // TODO fields, events
        }
    }
}
