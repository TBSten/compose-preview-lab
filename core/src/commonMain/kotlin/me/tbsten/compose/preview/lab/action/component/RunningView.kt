package me.tbsten.compose.preview.lab.action.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.progressindicators.CircularProgressIndicator

@Composable
internal fun RunningView() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(8.dp),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(12.dp),
            strokeWidth = 2.dp,
            color = PreviewLabTheme.colors.textSecondary,
        )

        val infiniteTransition = rememberInfiniteTransition()

        Text(
            text = "Running " +
                ".".repeat(
                    infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 3f + 0.999f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700),
                            repeatMode = RepeatMode.Restart,
                        ),
                    ).value.toInt(),
                ),
            style = PreviewLabTheme.typography.body2,
            color = PreviewLabTheme.colors.textSecondary,
        )
    }
}
