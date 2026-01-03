package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.SetField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.withEmptyHint
import me.tbsten.compose.preview.lab.field.withHint
import me.tbsten.compose.preview.lab.previewlab.PreviewLab

@Preview
@ComposePreviewLabOption(id = "SetFieldExample")
@Composable
internal fun SetFieldExample() = PreviewLab {
    Text(
        text = fieldValue {
            val initial = setOf("Apple", "Banana", "Cherry", "Date", "Elderberry")
            SetField<String>(
                label = "fruits",
                initialValue = initial,
                elementField = { StringField(label, initialValue) },
            ).withEmptyHint().withHint(
                "Initial" to initial,
                "1 item" to setOf("Apple"),
                "3 items" to setOf("Apple", "Banana", "Cherry"),
            )
        }.joinToString(", "),
    )
}
