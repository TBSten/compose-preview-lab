package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

/**
 * An outlined card whose content is split into sections separated by horizontal divider lines.
 * Shared by the navigation extensions' field views (`NavControllerField`, `NavBackStackField`).
 *
 * @param modifier Modifier for the card
 * @param content Content of the card, typically a few [PreviewLabSegmentedCardSection]s
 */
@InternalComposePreviewLabApi
@Composable
fun PreviewLabSegmentedCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    PreviewLabSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PreviewLabTheme.colors.outline),
        color = Color.Transparent,
    ) {
        Column {
            content()
        }
    }
}

/**
 * A section within a [PreviewLabSegmentedCard]. Non-top sections are separated from the previous
 * one by a horizontal divider line.
 *
 * @param isTop Whether this is the topmost section (no divider above)
 * @param modifier Modifier for the section
 * @param content Content of the section
 */
@InternalComposePreviewLabApi
@Composable
fun PreviewLabSegmentedCardSection(isTop: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isTop) {
                    Modifier.padding(top = 1.dp)
                } else {
                    Modifier
                },
            ),
    ) {
        if (!isTop) {
            PreviewLabSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = PreviewLabTheme.colors.outline,
            ) {}
        }
        Box(
            modifier = Modifier.padding(12.dp),
        ) {
            Column {
                content()
            }
        }
    }
}
