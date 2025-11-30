package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.CombinedField
import me.tbsten.compose.preview.lab.field.DpField
import me.tbsten.compose.preview.lab.field.combined
import me.tbsten.compose.preview.lab.field.splitedOf
import org.jetbrains.compose.ui.tooling.preview.Preview

internal data class CombinedFieldPadding(val horizontal: Dp, val vertical: Dp)

@Preview
@ComposePreviewLabOption(id = "CombinedFieldExample")
@Composable
internal fun CombinedFieldExample() = PreviewLab {
    val padding = fieldValue {
        CombinedField(
            label = "Padding",
            fields = listOf(
                DpField("Horizontal", 16.dp),
                DpField("Vertical", 8.dp)
            ),
            combine = { values ->
                @Suppress("UNCHECKED_CAST")
                CombinedFieldPadding(values[0] as Dp, values[1] as Dp)
            },
            split = { listOf(it.horizontal, it.vertical) }
        )
    }

    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .padding(horizontal = padding.horizontal, vertical = padding.vertical)
    ) {
        Text("Content")
    }
}

@Preview
@ComposePreviewLabOption(id = "CombinedFieldWithCombinedFunctionExample")
@Composable
internal fun CombinedFieldWithCombinedFunctionExample() = PreviewLab {
    val padding = fieldValue {
        combined(
            label = "Padding",
            field1 = DpField("Horizontal", 16.dp),
            field2 = DpField("Vertical", 8.dp),
            combine = { h, v -> CombinedFieldPadding(h, v) },
            split = { splitedOf(it.horizontal, it.vertical) }
        )
    }

    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .padding(horizontal = padding.horizontal, vertical = padding.vertical)
    ) {
        Text("Content")
    }
}
