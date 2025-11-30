package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.IntField
import me.tbsten.compose.preview.lab.field.NumberField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "IntFieldExample")
@Composable
internal fun IntFieldExample() = PreviewLab {
    Counter(
        count = fieldValue { IntField("Count", 0) },
    )
}

@Preview
@ComposePreviewLabOption(id = "IntFieldWithPrefixSuffixExample")
@Composable
internal fun IntFieldWithPrefixSuffixExample() = PreviewLab {
    Counter(
        count = fieldValue {
            IntField(
                label = "Count",
                initialValue = 0,
                inputType = NumberField.InputType.TextField(
                    prefix = { Text("$") },
                    suffix = { Text("yen") }
                )
            )
        },
    )
}

@Composable
internal fun Counter(count: Int) {
    Text("Count: $count")
}
