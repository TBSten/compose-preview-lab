package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.component.CommonMenu
import me.tbsten.compose.preview.lab.component.TransformableTextField
import me.tbsten.compose.preview.lab.component.Transformer
import me.tbsten.compose.preview.lab.component.colorpicker.CommonColorPicker
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Surface
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.util.thenIfNotNull

@Composable
internal fun DefaultModifierFieldValueBuilder(
    modifierTextCode: AnnotatedString,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    menuContent: (@Composable DefaultModifierFieldValueBuilderMenuScope.() -> Unit)? = null,
) {
    var isMenuOpen by remember { mutableStateOf(false) }

    Column {
        val shape = RoundedCornerShape(4.dp)
        Box(
            modifier = Modifier
                .clip(shape = shape)
                .thenIfNotNull(menuContent) { clickable { isMenuOpen = !isMenuOpen } }
                .border(1.dp, PreviewLabTheme.colors.outline, shape = shape)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp, horizontal = 8.dp),
        ) {
            Text(
                text = modifierTextCode,
                inlineContent = inlineContent,
                style = PreviewLabTheme.typography.body3,
                modifier = Modifier.wrapContentSize(),
            )
        }

        if (menuContent != null) {
            CommonMenu(
                expanded = isMenuOpen,
                onDismissRequest = { isMenuOpen = false },
            ) {
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(focusRequester) {
                    focusRequester.requestFocus()
                }

                Surface(
                    color = PreviewLabTheme.colors.background,
                    contentColor = PreviewLabTheme.colors.onBackground,
                    modifier = Modifier.focusable().focusRequester(focusRequester),
                ) {
                    menuContent(DefaultModifierFieldValueBuilderMenuScope())
                }
            }
        }
    }
}

class DefaultModifierFieldValueBuilderMenuScope internal constructor()

@Composable
internal fun DefaultModifierFieldValueBuilderMenuScope.DefaultMenu(
    content: @Composable DefaultModifierFieldValueBuilderDefaultMenuScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 8.dp),
    ) {
        content(DefaultModifierFieldValueBuilderDefaultMenuScope())
    }
}

class DefaultModifierFieldValueBuilderDefaultMenuScope {
    fun Modifier.menuItemPadding() = then(Modifier.padding(horizontal = 12.dp))
}

@Composable
fun DefaultModifierFieldValueBuilderDefaultMenuScope.DefaultMenuItem(label: String, content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .menuItemPadding()
            .fillMaxWidth(),
    ) {
        Label(label)

        Box(modifier = Modifier.weight(1f).defaultMinSize(minWidth = 100.dp)) {
            content()
        }
    }
}

@Composable
fun DefaultModifierFieldValueBuilderDefaultMenuScope.Label(label: AnnotatedString) = Text(
    text = label,
    style = PreviewLabTheme.typography.label1,
    modifier = Modifier.width(60.dp),
)

@Composable
fun DefaultModifierFieldValueBuilderDefaultMenuScope.Label(label: String) = Label(
    label = buildAnnotatedString { append(label) },
)

@Composable
fun <Value> DefaultModifierFieldValueBuilderDefaultMenuScope.TextFieldItem(
    label: String,
    value: Value,
    onValueChange: (Value) -> Unit,
    transformer: Transformer<Value>,
    suffix: String? = null,
) {
    DefaultMenuItem(
        label = label,
    ) {
        TransformableTextField(
            value = value,
            onValueChange = onValueChange,
            transformer = transformer,
            textStyle = PreviewLabTheme.typography.label1,
            suffix = suffix?.let { { Text(it, style = PreviewLabTheme.typography.label2) } },
            modifier = Modifier.defaultMinSize(minWidth = 120.dp),
        )
    }
}

@Composable
fun DefaultModifierFieldValueBuilderDefaultMenuScope.ColorPickerItem(
    label: String,
    value: Color,
    onValueChange: (Color) -> Unit,
) {
    DefaultMenuItem(
        label = label,
    ) {
        CommonColorPicker(
            color = value,
            onColorSelected = onValueChange,
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(16f / 9f),
        )
    }
}
