package me.tbsten.compose.preview.lab.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.core.rememberMenuState
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_check
import me.tbsten.compose.preview.lab.ui.LocalTextStyle
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Icon
import me.tbsten.compose.preview.lab.ui.components.Text
import org.jetbrains.compose.resources.imageResource

@Composable
internal fun <V> SelectButton(
    choices: List<V>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    title: (V) -> String,
    modifier: Modifier = Modifier,
    itemDetail: (V) -> String? = { null },
) = SelectButton(
    value = choices[currentIndex],
    choices = choices,
    onSelect = { onSelect(choices.indexOf(it)) },
    title = title,
    modifier = modifier,
    itemDetail = itemDetail,
)

@Composable
internal fun <V> SelectButton(
    value: V,
    choices: List<V>,
    onSelect: (V) -> Unit,
    title: (V) -> String,
    modifier: Modifier = Modifier,
    itemDetail: (V) -> String? = { null },
) {
    val menuState = rememberMenuState(expanded = false)

    CommonMenu(
        state = menuState,
    ) {
        Button(
            variant = ButtonVariant.PrimaryOutlined,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            onClick = { menuState.expanded = true },
            modifier = modifier,
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides PreviewLabTheme.typography.label3,
            ) {
                Text(title(value))
            }
        }

        CommonMenuContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                choices.forEachIndexed { index, item ->
                    val isSelected = item == value

                    CommonMenuItem(
                        onClick = {
                            onSelect(item)
                            menuState.expanded = false
                        },
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (isSelected) {
                            Icon(
                                imageResource(Res.drawable.icon_check),
                                contentDescription = "selected",
                            )
                        }

                        Column(
                            Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = title(item),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = PreviewLabTheme.typography.body3,
                            )

                            itemDetail(item)?.let {
                                Text(
                                    text = it,
                                    style = PreviewLabTheme.typography.body3,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
