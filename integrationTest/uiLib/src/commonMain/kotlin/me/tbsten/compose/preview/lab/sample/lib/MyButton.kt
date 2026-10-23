package me.tbsten.compose.preview.lab.sample.lib

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.gallery.LocalPreviewLabGalleryNavigator
import me.tbsten.compose.preview.lab.gallery.navigateOr
import me.tbsten.compose.preview.lab.sample.debugmenu.AppDebugMenu

@Composable
fun MyButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = {
            AppDebugMenu.logger.debug("MyButton.onClick")
            onClick()
        },
        modifier = modifier,
    ) {
        Text(text = text)
    }
}

/**
 * This is a MyButtonPreview KDoc.
 *
 * @see [MyButtonPreview]
 */
@ComposePreviewLabOption(
    id = "MyButtonPreview",
    displayName = "UI Component in library module Preview",
)
@Preview
@Composable
private fun MyButtonPreview() = CustomizedPreviewLab {
    val previewLabGalleryNavigator = LocalPreviewLabGalleryNavigator.current

    MyButton(
        text = fieldValue { StringField("MyButton.text", "Click Me") },
        onClick = {
            previewLabGalleryNavigator.navigateOr(
                id = "MyTextFieldPreview",
                fallback = { onEvent("MyButton.onClick") },
            )
        },
        modifier = Modifier.padding(20.dp),
    )
}
