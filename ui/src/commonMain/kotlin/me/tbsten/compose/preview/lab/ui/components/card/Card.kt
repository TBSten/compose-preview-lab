package me.tbsten.compose.preview.lab.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.LocalTypography
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabSurface

@Composable
@UiComposePreviewLabApi
fun PreviewLabCard(
    modifier: Modifier = Modifier,
    shape: Shape = PreviewLabCardDefaults.Shape,
    colors: PreviewLabCardColors = PreviewLabCardDefaults.cardColors(),
    elevation: CardElevation = PreviewLabCardDefaults.cardElevation(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    PreviewLabSurface(
        modifier = modifier,
        shape = shape,
        color = colors.containerColor(enabled = true).value,
        contentColor = colors.contentColor(enabled = true).value,
        shadowElevation =
        elevation.shadowElevation(
            enabled = true,
            interactionSource = null,
        ).value,
        border = border,
    ) {
        Column(content = content)
    }
}

@Composable
@UiComposePreviewLabApi
fun PreviewLabCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = PreviewLabCardDefaults.Shape,
    colors: PreviewLabCardColors = PreviewLabCardDefaults.cardColors(),
    elevation: CardElevation = PreviewLabCardDefaults.cardElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) {
    PreviewLabSurface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = colors.containerColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        shadowElevation = elevation.shadowElevation(enabled, interactionSource).value,
        border = border,
        interactionSource = interactionSource,
    ) {
        Column(content = content)
    }
}

@Composable
@UiComposePreviewLabApi
fun PreviewLabElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = PreviewLabCardDefaults.ElevatedShape,
    colors: PreviewLabCardColors = PreviewLabCardDefaults.elevatedCardColors(),
    elevation: CardElevation = PreviewLabCardDefaults.elevatedCardElevation(),
    content: @Composable ColumnScope.() -> Unit,
) = PreviewLabCard(
    modifier = modifier,
    shape = shape,
    border = null,
    elevation = elevation,
    colors = colors,
    content = content,
)

@Composable
@UiComposePreviewLabApi
fun PreviewLabElevatedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = PreviewLabCardDefaults.ElevatedShape,
    colors: PreviewLabCardColors = PreviewLabCardDefaults.elevatedCardColors(),
    elevation: CardElevation = PreviewLabCardDefaults.elevatedCardElevation(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) = PreviewLabCard(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = null,
    interactionSource = interactionSource,
    content = content,
)

@Composable
@UiComposePreviewLabApi
fun PreviewLabOutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = PreviewLabCardDefaults.OutlinedShape,
    colors: PreviewLabCardColors = PreviewLabCardDefaults.outlinedCardColors(),
    elevation: CardElevation = PreviewLabCardDefaults.outlinedCardElevation(),
    border: BorderStroke = PreviewLabCardDefaults.outlinedCardBorder(),
    content: @Composable ColumnScope.() -> Unit,
) = PreviewLabCard(
    modifier = modifier,
    shape = shape,
    border = border,
    elevation = elevation,
    colors = colors,
    content = content,
)

@Composable
@UiComposePreviewLabApi
fun PreviewLabOutlinedCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = PreviewLabCardDefaults.OutlinedShape,
    colors: PreviewLabCardColors = PreviewLabCardDefaults.outlinedCardColors(),
    elevation: CardElevation = PreviewLabCardDefaults.outlinedCardElevation(),
    border: BorderStroke = PreviewLabCardDefaults.outlinedCardBorder(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) = PreviewLabCard(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = border,
    interactionSource = interactionSource,
    content = content,
)

@UiComposePreviewLabApi
object PreviewLabCardDefaults {
    val Shape: Shape @Composable get() = RoundedCornerShape(12.0.dp)
    val ElevatedShape: Shape @Composable get() = Shape
    val OutlinedShape: Shape @Composable get() = Shape
    val BorderWidth = 1.dp

    @Composable
    fun cardElevation(
        defaultElevation: Dp = 0.0.dp,
        pressedElevation: Dp = 0.0.dp,
        focusedElevation: Dp = 0.0.dp,
        hoveredElevation: Dp = 1.0.dp,
        draggedElevation: Dp = 3.0.dp,
        disabledElevation: Dp = 0.0.dp,
    ): CardElevation = CardElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        draggedElevation = draggedElevation,
        disabledElevation = disabledElevation,
    )

    @Composable
    fun elevatedCardElevation(
        defaultElevation: Dp = 2.0.dp,
        pressedElevation: Dp = 4.0.dp,
        focusedElevation: Dp = 4.0.dp,
        hoveredElevation: Dp = 4.0.dp,
        draggedElevation: Dp = 4.0.dp,
        disabledElevation: Dp = 0.0.dp,
    ): CardElevation = CardElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        draggedElevation = draggedElevation,
        disabledElevation = disabledElevation,
    )

    @Composable
    fun outlinedCardElevation(
        defaultElevation: Dp = 0.0.dp,
        pressedElevation: Dp = 0.0.dp,
        focusedElevation: Dp = 0.0.dp,
        hoveredElevation: Dp = 1.0.dp,
        draggedElevation: Dp = 3.0.dp,
        disabledElevation: Dp = 0.0.dp,
    ): CardElevation = CardElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        draggedElevation = draggedElevation,
        disabledElevation = disabledElevation,
    )

    @Composable
    fun cardColors(
        containerColor: Color = PreviewLabTheme.colors.surface,
        contentColor: Color = PreviewLabTheme.colors.onSurface,
        disabledContainerColor: Color =
            PreviewLabTheme.colors.disabled,
        disabledContentColor: Color = PreviewLabTheme.colors.onDisabled,
    ): PreviewLabCardColors = PreviewLabCardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    @Composable
    fun elevatedCardColors(
        containerColor: Color = PreviewLabTheme.colors.background,
        contentColor: Color = PreviewLabTheme.colors.onBackground,
        disabledContainerColor: Color =
            PreviewLabTheme.colors.disabled,
        disabledContentColor: Color = PreviewLabTheme.colors.onDisabled,
    ): PreviewLabCardColors = PreviewLabCardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    @Composable
    fun outlinedCardColors(
        containerColor: Color = PreviewLabTheme.colors.background,
        contentColor: Color = PreviewLabTheme.colors.onBackground,
        disabledContainerColor: Color =
            PreviewLabTheme.colors.disabled,
        disabledContentColor: Color = PreviewLabTheme.colors.onDisabled,
    ): PreviewLabCardColors = PreviewLabCardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )

    @Composable
    fun outlinedCardBorder(
        enabled: Boolean = true,
        color: Color =
            if (enabled) {
                PreviewLabTheme.colors.outline
            } else {
                PreviewLabTheme.colors.disabled
            },
        borderWidth: Dp = BorderWidth,
    ): BorderStroke = remember(borderWidth, color) { BorderStroke(borderWidth, color) }
}

@ConsistentCopyVisibility
@Immutable
@UiComposePreviewLabApi
data class PreviewLabCardColors @UiComposePreviewLabApi
constructor(
    private val containerColor: Color,
    private val contentColor: Color,
    private val disabledContainerColor: Color,
    private val disabledContentColor: Color,
) {
    @Composable
    @UiComposePreviewLabApi
    fun containerColor(enabled: Boolean): State<Color> =
        rememberUpdatedState(if (enabled) containerColor else disabledContainerColor)

    @Composable
    @UiComposePreviewLabApi
    fun contentColor(enabled: Boolean): State<Color> = rememberUpdatedState(if (enabled) contentColor else disabledContentColor)
}

@Composable
@Preview(showBackground = true)
private fun CardComponentSampleInTheme() {
    PreviewLabTheme {
        CardComponentSample()
    }
}

@Composable
@Preview(showBackground = true)
private fun CardComponentSample() {
    val cardModifier =
        Modifier
            .fillMaxWidth()
            .height(120.dp)

    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column {
            BasicText(text = "Default Card", style = LocalTypography.current.h3)
            Spacer(modifier = Modifier.height(8.dp))

            PreviewLabCard(
                modifier = cardModifier,
            ) {}
        }

        Column {
            BasicText(text = "Elevated Card with Action", style = LocalTypography.current.h3)
            PreviewLabElevatedCard(
                modifier = cardModifier,
                onClick = { /* Handle click */ },
            ) {}
        }

        // Outlined Card
        Column {
            BasicText(text = "Custom Outlined Card", style = LocalTypography.current.h3)
            PreviewLabOutlinedCard(
                modifier = cardModifier,
            ) {}
        }

        Column {
            BasicText(text = "Disabled Card", style = LocalTypography.current.h3)
            PreviewLabCard(
                modifier = cardModifier,
                onClick = { },
                enabled = false,
                colors =
                PreviewLabCardDefaults.cardColors(
                    containerColor = Color(0xFFBDBDBD),
                    contentColor = Color(0xFF9E9E9E),
                    disabledContainerColor = Color(0xFFEEEEEE),
                    disabledContentColor = Color(0xFFBDBDBD),
                ),
            ) {}
        }

        Column {
            BasicText(text = "Custom Colored Card", style = LocalTypography.current.h3)
            PreviewLabCard(
                modifier = cardModifier,
                colors =
                PreviewLabCardDefaults.cardColors(
                    containerColor = Color(0xFFECEFF1),
                    contentColor = Color(0xFF607D8B),
                ),
            ) {}
        }

        Column {
            BasicText(text = "Outlined Card with Hover Elevation", style = LocalTypography.current.h3)
            PreviewLabOutlinedCard(
                modifier = cardModifier,
                onClick = { /* Handle click */ },
                elevation =
                PreviewLabCardDefaults.outlinedCardElevation(
                    defaultElevation = 0.dp,
                    hoveredElevation = 4.dp,
                ),
                border = BorderStroke(1.dp, Color(0xFFBDBDBD)),
                colors =
                PreviewLabCardDefaults.outlinedCardColors(
                    containerColor = Color(0xFFE0E0E0),
                    contentColor = Color(0xFF616161),
                ),
            ) {}
        }

        // Interactive Card
        Column {
            BasicText(text = "Interactive Card", style = LocalTypography.current.h3)
            PreviewLabCard(
                modifier = cardModifier,
                onClick = { /* Handle click */ },
                colors =
                PreviewLabCardDefaults.cardColors(
                    containerColor = Color(0xFFECEFF1),
                    contentColor = Color(0xFF455A64),
                ),
                enabled = true,
            ) {}
        }
    }
}
