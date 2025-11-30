package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.nullable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "NullableFieldExample")
@Composable
internal fun NullableFieldExample() = PreviewLab {
    val userName = fieldValue {
        StringField("User Name", "John Doe").nullable()
    }

    UserName(userName = userName)
}

@Composable
internal fun UserName(userName: String?) {
    Text(userName ?: "No user name")
}
