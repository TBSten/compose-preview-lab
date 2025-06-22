package me.tbsten.compose.preview.lab.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.LabPreview
import me.tbsten.compose.preview.lab.PreviewLab
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
