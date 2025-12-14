package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.previewlab.generated.resources.Res
import me.tbsten.compose.preview.lab.previewlab.generated.resources.icon_right_panel_close
import me.tbsten.compose.preview.lab.previewlab.generated.resources.icon_right_panel_open
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Icon
import me.tbsten.compose.preview.lab.ui.components.Text
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun InspectorPanelVisible(isInspectorPanelVisible: Boolean, onToggle: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        modifier = Modifier
            .semantics(mergeDescendants = true) { }
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggle() }
            .padding(8.dp),
    ) {
        Icon(
            painter = painterResource(
                if (isInspectorPanelVisible) {
                    Res.drawable.icon_right_panel_close
                } else {
                    Res.drawable.icon_right_panel_open
                },
            ),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )

        Text(
            text = "Toggle\nTabs",
            style = PreviewLabTheme.typography.label3,
            textAlign = TextAlign.Center,
        )
    }
}
