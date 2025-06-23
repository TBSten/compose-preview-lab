package me.tbsten.compose.preview.lab.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.composables.core.Menu
import com.composables.core.MenuContent
import com.composables.core.MenuItem
import com.composables.core.MenuScope
import com.composables.core.MenuState
import me.tbsten.compose.preview.lab.ui.components.card.CardDefaults
import me.tbsten.compose.preview.lab.ui.components.card.ElevatedCard
import me.tbsten.compose.preview.lab.ui.foundation.ripple

@Composable
internal fun CommonMenu(state: MenuState, modifier: Modifier = Modifier, content: @Composable (MenuScope.() -> Unit)) = Menu(
    state = state,
    modifier = modifier,
    content = content,
)

@Composable
internal fun MenuScope.CommonMenuContent(
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn(tween(250)),
    exit: ExitTransition = fadeOut(tween(200)),
    alignment: Alignment.Horizontal = Alignment.Start,
    contents: @Composable (() -> Unit),
) = MenuContent(
    modifier = modifier,
    enter = enter,
    exit = exit,
    alignment = alignment,
) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 8.dp,
            draggedElevation = 8.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        Box(Modifier.width(IntrinsicSize.Min).padding(8.dp)) {
            contents()
        }
    }
}

@Composable
internal fun MenuScope.CommonMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication = ripple(),
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
    shape: Shape = RoundedCornerShape(4.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    contents: @Composable (RowScope.() -> Unit),
) = MenuItem(
    onClick = onClick,
    enabled = enabled,
    interactionSource = interactionSource,
    indication = indication,
    contentPadding = contentPadding,
    shape = shape,
    horizontalArrangement = horizontalArrangement,
    verticalAlignment = verticalAlignment,
    modifier = modifier.fillMaxWidth(),
) {
    Row(Modifier.fillMaxWidth().width(IntrinsicSize.Max)) {
        contents()
    }
}
