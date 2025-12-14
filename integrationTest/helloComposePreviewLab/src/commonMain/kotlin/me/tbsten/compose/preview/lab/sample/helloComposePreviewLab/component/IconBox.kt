package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A styled box component that displays a label or icon with a colored background.
 * Used as a Web-compatible alternative to emoji icons in documentation.
 *
 * @param color Background color of the box
 * @param label Text to display inside the box (used when icon is null)
 * @param icon Optional icon to display instead of label
 * @param modifier Optional modifier for the box
 */
@Composable
internal fun IconBox(
    color: Color,
    label: String = "",
    icon: Painter? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Image(
                painter = icon,
                contentDescription = label.ifEmpty { null },
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.size(20.dp),
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
