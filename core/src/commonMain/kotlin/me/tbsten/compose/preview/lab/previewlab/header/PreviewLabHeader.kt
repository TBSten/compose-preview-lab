package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Divider

@Composable
internal fun PreviewLabHeader(
    isHeaderShow: Boolean,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    isInspectorPanelVisible: Boolean,
    modifier: Modifier = Modifier,
    onIsInspectorPanelVisibleToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.clip(RectangleShape)) {
        if (isHeaderShow) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .background(PreviewLabTheme.colors.background)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp)
                    .zIndex(2f),
            ) {
                Zoom(
                    scale = scale,
                    onScaleChange = onScaleChange,
                )

                InspectorPanelVisible(
                    isInspectorPanelVisible = isInspectorPanelVisible,
                    onToggle = onIsInspectorPanelVisibleToggle,
                )
            }

            Divider(
                Modifier
                    .zIndex(2f),
            )
        }

        content()
    }
}
