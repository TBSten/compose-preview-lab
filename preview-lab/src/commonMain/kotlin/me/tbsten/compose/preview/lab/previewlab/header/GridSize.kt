package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabCheckbox
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.components.textfield.PreviewLabOutlinedTextField

@Composable
internal fun GridSize(gridSize: Dp?, onGridSizeChange: (Dp?) -> Unit) {
    var isGrid by remember { mutableStateOf(true) }
    var textFieldValue by remember(gridSize) { mutableStateOf(gridSize?.value?.toString() ?: "") }

    Column {
        PreviewLabText("Grid", style = PreviewLabTheme.typography.label2)

        Row {
            PreviewLabCheckbox(
                checked = isGrid,
                onCheckedChange = {
                    isGrid = it
                    onGridSizeChange(
                        if (it) {
                            gridSize ?: 40.dp
                        } else {
                            null
                        },
                    )
                },
            )

            AnimatedVisibility(
                visible = isGrid,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
            ) {
                PreviewLabOutlinedTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        textFieldValue.toDoubleOrNull()?.let {
                            onGridSizeChange(it.dp)
                        }
                    },
                    isError = (isGrid && textFieldValue.toDoubleOrNull().let { it == null || it <= 0 }),
                    suffix = { PreviewLabText(".dp") },
                    textStyle = PreviewLabTheme.typography.input.copy(textAlign = TextAlign.End),
                    singleLine = true,
                    enabled = isGrid,
                    modifier = Modifier.width(100.dp),
                )
            }
        }
    }
}
