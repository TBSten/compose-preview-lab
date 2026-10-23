package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_arrow_drop_down
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_arrow_left
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_arrow_right
import org.jetbrains.compose.resources.DrawableResource

/**
 * Position of the debug menu drawer.
 */
enum class DebugMenuDrawerPosition {
    /** Drawer slides in from the right side */
    Right,

    /** Drawer slides in from the left side */
    Left,

    /** Drawer slides in from the bottom */
    Bottom,
}

internal val DebugMenuDrawerPosition.alignment: Alignment
    get() = when (this) {
        DebugMenuDrawerPosition.Right -> Alignment.CenterEnd
        DebugMenuDrawerPosition.Left -> Alignment.CenterStart
        DebugMenuDrawerPosition.Bottom -> Alignment.BottomCenter
    }

internal val DebugMenuDrawerPosition.shape: Shape
    get() = when (this) {
        DebugMenuDrawerPosition.Right -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
        DebugMenuDrawerPosition.Left -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
        DebugMenuDrawerPosition.Bottom -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    }

internal fun DebugMenuDrawerPosition.sizeModifier(size: Dp): Modifier = when (this) {
    DebugMenuDrawerPosition.Right, DebugMenuDrawerPosition.Left ->
        Modifier.fillMaxHeight().width(size)
    DebugMenuDrawerPosition.Bottom ->
        Modifier.fillMaxWidth().height(size)
}

internal val DebugMenuDrawerPosition.enterTransition: EnterTransition
    get() = when (this) {
        DebugMenuDrawerPosition.Right -> slideInHorizontally(initialOffsetX = { it })
        DebugMenuDrawerPosition.Left -> slideInHorizontally(initialOffsetX = { -it })
        DebugMenuDrawerPosition.Bottom -> slideInVertically(initialOffsetY = { it })
    }

internal val DebugMenuDrawerPosition.exitTransition: ExitTransition
    get() = when (this) {
        DebugMenuDrawerPosition.Right -> slideOutHorizontally(targetOffsetX = { it })
        DebugMenuDrawerPosition.Left -> slideOutHorizontally(targetOffsetX = { -it })
        DebugMenuDrawerPosition.Bottom -> slideOutVertically(targetOffsetY = { it })
    }

internal val DebugMenuDrawerPosition.iconRes: DrawableResource
    get() = when (this) {
        DebugMenuDrawerPosition.Right -> PreviewLabUiRes.drawable.icon_arrow_right
        DebugMenuDrawerPosition.Left -> PreviewLabUiRes.drawable.icon_arrow_left
        DebugMenuDrawerPosition.Bottom -> PreviewLabUiRes.drawable.icon_arrow_drop_down
    }

internal val DebugMenuDrawerPosition.label: String
    get() = when (this) {
        DebugMenuDrawerPosition.Right -> "Right"
        DebugMenuDrawerPosition.Left -> "Left"
        DebugMenuDrawerPosition.Bottom -> "Bottom"
    }
