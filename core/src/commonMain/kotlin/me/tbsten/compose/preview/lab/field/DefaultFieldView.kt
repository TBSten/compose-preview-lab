package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * Default UI implementation of [PreviewLabField.View]. Display a label and draw the content below it.
 *
 * Use [PreviewLabField.Content] to customize the UI of the field.
 * [PreviewLabField.View] only if you want to customize the UI, including the part that displays the label, using this Composable.
 *
 * @see PreviewLabField
 */
@Composable
fun <Value> PreviewLabField<Value>.DefaultFieldView(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = { Content() },
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        FieldLabelHeader()
        content()
    }
}

/**
 * Display the label of [PreviewLabField].
 *
 * @see DefaultFieldView
 * @see PreviewLabField
 */
@Composable
fun PreviewLabField<*>.FieldLabelHeader() {
    Text(
        text = this.label,
        style = PreviewLabTheme.typography.label2,
    )
}
