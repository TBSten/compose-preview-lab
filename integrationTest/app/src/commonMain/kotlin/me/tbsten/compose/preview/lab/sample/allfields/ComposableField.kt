package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.ComposableField
import me.tbsten.compose.preview.lab.field.ComposableFieldValue
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "ComposableFieldExample")
@Composable
internal fun ComposableFieldExample() = PreviewLab {
    val content = fieldValue {
        ComposableField(
            label = "Content",
            initialValue = ComposableFieldValue.Red32X32
        )
    }

    ComposableFieldMyContainer(content = content)
}

@Preview
@ComposePreviewLabOption(id = "ComposableFieldWithPredefinedValuesExample")
@Composable
internal fun ComposableFieldWithPredefinedValuesExample() = PreviewLab {
    val content = fieldValue {
        ComposableField(
            label = "Content",
            initialValue = ComposableFieldValue.Red32X32,
            choices = listOf(
                ComposableFieldValue.Red32X32,
                ComposableFieldValue.SimpleText,
                ComposableFieldValue.Empty
            )
        )
    }

    ComposableFieldMyContainer(content = content)
}

@Composable
internal fun ComposableFieldMyContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
