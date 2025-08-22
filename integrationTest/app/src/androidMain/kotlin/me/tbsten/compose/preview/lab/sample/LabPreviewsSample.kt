package me.tbsten.compose.preview.lab.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.LabPreview
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.AnnotatedStringField
import me.tbsten.compose.preview.lab.field.ColorField
import me.tbsten.compose.preview.lab.field.ComposableField
import me.tbsten.compose.preview.lab.field.ModifierField
import me.tbsten.compose.preview.lab.field.PathField
import me.tbsten.compose.preview.lab.field.ShapeField
import me.tbsten.compose.preview.lab.field.StringField

@LabPreview
@Composable
fun Test() = PreviewLab(maxWidth = 800.dp, maxHeight = 800.dp) {
    Text(fieldValue { StringField("text", "hoge") }, modifier = Modifier.fillMaxSize())
}

@LabPreview.Small
@Composable
fun Test1() = PreviewLab {
    Text(fieldValue { StringField("text", "hoge") })
}

@LabPreview.Medium
@Composable
fun Test2() = PreviewLab {
    Text(fieldValue { StringField("text", "hoge") })
}

@LabPreview.Large
@Composable
fun Test3() = PreviewLab {
    Text(fieldValue { StringField("text", "hoge") })
}

@LabPreview
@ComposePreviewLabOption(
    displayName = "New Fields Preview"
)
@Composable
fun NewFieldsPreview() = PreviewLab {
    Column(
        modifier = fieldValue { ModifierField("columnModifier", Modifier.padding(16.dp)) }
    ) {
        val color = fieldValue { ColorField("backgroundColor", Color.Yellow) }
        val shape = fieldValue { ShapeField("shape", CircleShape) }
        val annotatedString = fieldValue { AnnotatedStringField("annotatedString", AnnotatedString("Hello")) }
        val path = fieldValue { PathField("path", Path()) }

        Box(
            modifier = Modifier
                .background(color, shape)
                .padding(16.dp)
        ) {
            Text(annotatedString)
        }

        fieldValue { ComposableField("slot") { Text("Slot Content") } }
    }
}
