package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.DoubleField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "DoubleFieldExample")
@Composable
internal fun DoubleFieldExample() = PreviewLab {
    PriceTag(
        price = fieldValue { DoubleField("Price", 99.99) },
    )
}

@Composable
internal fun PriceTag(price: Double) {
    Text("Price: $$price")
}
