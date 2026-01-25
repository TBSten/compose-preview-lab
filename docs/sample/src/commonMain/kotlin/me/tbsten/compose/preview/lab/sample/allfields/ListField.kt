package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.ListField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.withEmptyHint
import me.tbsten.compose.preview.lab.field.withHint
import me.tbsten.compose.preview.lab.previewlab.PreviewLab

/**
 * Demonstrates [ListField] for editing a dynamic list of values.
 *
 * Add, remove, or reorder string items in the list.
 * Includes hints for quick population (1, 10, or 100 items).
 */
@Preview
@ComposePreviewLabOption(id = "ListFieldExample")
@Composable
internal fun ListFieldExample() = PreviewLab {
    Text(
        text = fieldValue {
            val initial = List(100) {
                "Character $it".repeat(if (it > 1 && it % 10 == 0) 20 else 1)
            }
            ListField<String>(
                label = "characters",
                initialValue = initial,
                elementField = { StringField(label, initialValue) },
            ).withHint(
                "Initial" to initial,
                "1 items" to initial.take(1),
                "10 items" to initial.take(10),
            ).withEmptyHint()
        }.joinToString(", "),
    )
}
