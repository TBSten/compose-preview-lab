package me.tbsten.compose.preview.lab.field.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.card.OutlinedCard

@Composable
@InternalComposePreviewLabApi
internal fun CollectionFieldSummaryCard(summaryText: String, onClick: () -> Unit, modifier: Modifier = Modifier,) {
    OutlinedCard(
        shape = RoundedCornerShape(4.dp),
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
            Text(
                text = summaryText,
                style = PreviewLabTheme.typography.body3,
                overflow = TextOverflow.Ellipsis,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            )
        }
    }
}
