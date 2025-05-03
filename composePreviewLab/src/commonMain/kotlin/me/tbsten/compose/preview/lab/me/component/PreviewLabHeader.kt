package me.tbsten.compose.preview.lab.me.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.me.PreviewLabConfiguration

@Composable
internal fun PreviewLabHeader(
    configurations: List<PreviewLabConfiguration>,
    scale: Float,
    onScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    initialConfigurationIndex: Int = 0,
    content: @Composable (PreviewLabConfiguration) -> Unit
) {
    var selectedConfigurationIndex by remember(initialConfigurationIndex) {
        mutableStateOf(
            initialConfigurationIndex
        )
    }

    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .height(IntrinsicSize.Min)
                .padding(8.dp)
        ) {
            Zoom(
                scale = scale,
                onScaleChange = onScaleChange,
                modifier = Modifier.fillMaxHeight()
            )

            if (2 <= configurations.size) {
                Divider()

                SelectConfiguration(
                    configurations = configurations,
                    selectedConfigurationIndex = selectedConfigurationIndex,
                    onSelect = { selectedConfigurationIndex = it },
                )
            }
        }

        Divider()

        AnimatedContent(
            targetState = selectedConfigurationIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.zIndex(-1f).weight(1f).fillMaxWidth(),
        ) { selectedConfigurationIndex ->
            content(configurations[selectedConfigurationIndex])
        }
    }
}

@Composable
private fun Zoom(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Zoom", style = MaterialTheme.typography.labelMedium)

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CommonIconButton(
                imageVector = Icons.Default.ZoomIn,
                contentDescription = "Zoom In",
                enabled = scale < MaxZoomScale,
                onClick = {
                    onScaleChange(scale.nextZoomInScale())
                },
            )

            CommonIconButton(
                imageVector = Icons.Default.ZoomOut,
                contentDescription = "Zoom Out",
                enabled = MinZoomScale < scale,
                onClick = {
                    onScaleChange(scale.nextZoomOutScale())
                },
                modifier = Modifier.weight(1f)
            )

            CommonIconButton(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Zoom Reset",
                onClick = {
                    onScaleChange(1.00f)
                },
            )
        }
    }
}

@Composable
private fun SelectConfiguration(
    configurations: List<PreviewLabConfiguration>,
    selectedConfigurationIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(modifier = modifier) {
        Text("Configurations", style = MaterialTheme.typography.labelMedium)

        SelectButton(
            choices = configurations,
            currentIndex = selectedConfigurationIndex,
            onSelect = onSelect,
            title = { it.name },
            itemDetail = { conf ->
                "maxSize = ${conf.maxWidth ?: "fit-content"} × ${conf.maxHeight ?: "fit-content"}"
            },
        )
    }

}

private const val MinZoomScale = 0.10f
private const val MaxZoomScale = 10.00f
private fun Float.nextZoomInScale(): Float = when (this) {
    in Float.MIN_VALUE..<1.0f -> this + 0.10f
    in 1.0f..<2.0f -> this + 0.25f
    in 1.0f..<Float.MAX_VALUE -> this + 1.00f
    else -> TODO("Zoom value is out of range: $this")
}

private fun Float.nextZoomOutScale(): Float = when (this) {
    in Float.MIN_VALUE..<1.0f -> this - 0.10f
    in 1.0f..<2.0f -> this - 0.25f
    in 1.0f..<Float.MAX_VALUE -> this - 1.00f
    else -> TODO("Zoom value is out of range: $this")
}
