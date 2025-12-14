package me.tbsten.compose.preview.lab.sample

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.WrapRange
import me.tbsten.compose.preview.lab.field.wrap
import me.tbsten.compose.preview.lab.LocalIsInPreviewLabGalleryCardBody

/**
 * Observes value changes of a [State] and invokes [onValueChange] when the value changes.
 *
 * @param state The state to observe
 * @param onValueChange Callback invoked when the value changes (excludes initial value)
 */
@Composable
fun <T> OnValueChange(state: State<T>, onValueChange: (T) -> Unit) {
    LaunchedEffect(Unit) {
        snapshotFlow { state.value }
            .drop(1)
            .collect { onValueChange(it) }
    }
}

fun <Value> MutablePreviewLabField<Value>.speechBubble(
    visible: @Composable () -> Boolean,
    bubbleText: String,
    alignment: Alignment = Alignment.TopCenter,
    onClose: (() -> Unit)? = null,
) = wrap(wrapRange = WrapRange.Full) { content ->
    var isClosed by remember { mutableStateOf(false) }

    SpeechBubbleBox(
        bubbleText = bubbleText,
        visible = visible() && isClosed.not(),
        alignment = alignment,
        onClose = onClose?.let {
            {
                isClosed = true
                onClose.invoke()
            }
        },
        content = content,
    )
}

@Composable
internal fun SpeechBubbleBox(
    bubbleText: String,
    alignment: Alignment = Alignment.TopCenter,
    visible: Boolean = true,
    onClose: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) = SpeechBubbleBox(
    bubble = { Text(bubbleText) },
    alignment = alignment,
    visible = visible,
    onClose = onClose,
    content = content,
)

@Composable
internal fun SpeechBubbleBox(
    bubble: @Composable () -> Unit,
    alignment: Alignment,
    visible: Boolean = true,
    onClose: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (LocalIsInPreviewLabGalleryCardBody.current) {
        content()
        return
    }

    val tailPosition = when (alignment) {
        Alignment.TopStart -> TailPosition.BottomStart
        Alignment.TopCenter -> TailPosition.BottomCenter
        Alignment.TopEnd -> TailPosition.BottomEnd
        Alignment.BottomStart -> TailPosition.TopStart
        Alignment.BottomCenter -> TailPosition.TopCenter
        Alignment.BottomEnd -> TailPosition.TopEnd
        else -> TailPosition.BottomCenter
    }

    val transformOrigin = when (alignment) {
        Alignment.TopStart -> TransformOrigin(0f, 1f)
        Alignment.TopCenter -> TransformOrigin(0.5f, 1f)
        Alignment.TopEnd -> TransformOrigin(1f, 1f)
        Alignment.BottomStart -> TransformOrigin(0f, 0f)
        Alignment.BottomCenter -> TransformOrigin(0.5f, 0f)
        Alignment.BottomEnd -> TransformOrigin(1f, 0f)
        Alignment.CenterStart -> TransformOrigin(0f, 0.5f)
        Alignment.CenterEnd -> TransformOrigin(1f, 0.5f)
        else -> TransformOrigin.Center
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val hoverOffsetY = when {
        tailPosition.isBottom -> (-4).dp
        tailPosition.isTop -> 4.dp
        else -> 0.dp
    }

    val popupPositionProvider = remember(alignment) {
        SpeechBubblePositionProvider(alignment)
    }

    Box {
        content()

        Popup(
            popupPositionProvider = popupPositionProvider,
        ) {
            AnimatedVisibility(
                visibleState = remember { MutableTransitionState(false) }.apply {
                    LaunchedEffect(this, visible) {
                        delay(0.25.seconds)
                        targetState = visible
                    }
                },
                enter = fadeIn() + scaleIn(transformOrigin = transformOrigin),
                exit = fadeOut() + scaleOut(transformOrigin = transformOrigin),
            ) {
                SpeechBubble(
                    tailPosition = tailPosition,
                    interactionSource = interactionSource,
                    isHovered = isHovered,
                    hoverOffsetY = hoverOffsetY,
                    onClose = onClose,
                    content = bubble,
                )
            }
        }
    }
}

private class SpeechBubblePositionProvider(private val alignment: Alignment) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = when (alignment) {
            Alignment.TopStart, Alignment.BottomStart, Alignment.CenterStart ->
                anchorBounds.left
            Alignment.TopEnd, Alignment.BottomEnd, Alignment.CenterEnd ->
                anchorBounds.right - popupContentSize.width
            else ->
                anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        }

        val y = when (alignment) {
            Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd ->
                anchorBounds.top - popupContentSize.height
            Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd ->
                anchorBounds.bottom
            else ->
                anchorBounds.top - popupContentSize.height
        }

        return IntOffset(x, y)
    }
}

private enum class TailPosition {
    TopStart,
    TopCenter,
    TopEnd,
    BottomStart,
    BottomCenter,
    BottomEnd,
    ;

    val isTop: Boolean get() = this == TopStart || this == TopCenter || this == TopEnd
    val isBottom: Boolean get() = this == BottomStart || this == BottomCenter || this == BottomEnd
}

@Composable
private fun SpeechBubble(
    modifier: Modifier = Modifier,
    tailPosition: TailPosition = TailPosition.BottomCenter,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isHovered: Boolean = false,
    hoverOffsetY: Dp = 0.dp,
    onClose: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val bubbleShape = remember(tailPosition) { createBubbleShape(tailPosition) }
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isHovered) hoverOffsetY else 0.dp,
        label = "hoverOffset",
    )

    Box(
        modifier = modifier
            .hoverable(interactionSource)
            .offset(y = animatedOffsetY)
            .shadow(
                elevation = 8.dp,
                shape = bubbleShape,
                clip = false,
            )
            .clip(bubbleShape)
            .background(backgroundColor)
            .padding(
                start = 12.dp,
                end = if (onClose != null) 4.dp else 12.dp,
                top = if (tailPosition.isTop) 16.dp else 8.dp,
                bottom = if (tailPosition.isBottom) 16.dp else 8.dp,
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            content()
            if (onClose != null) {
                Spacer(Modifier.width(4.dp))
                TextButton(
                    onClick = onClose,
                ) {
                    Text(
                        text = "×",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

private fun createBubbleShape(tailPosition: TailPosition): Shape = GenericShape { size: Size, _: LayoutDirection ->
    val width = size.width
    val height = size.height
    val cornerRadius = 12f
    val tailWidth = 16f
    val tailHeight = 10f

    // Calculate the X coordinate of the tail
    val tailCenterX = when (tailPosition) {
        TailPosition.TopStart, TailPosition.BottomStart -> cornerRadius + tailWidth / 2 + 8f
        TailPosition.TopCenter, TailPosition.BottomCenter -> width / 2
        TailPosition.TopEnd, TailPosition.BottomEnd -> width - cornerRadius - tailWidth / 2 - 8f
    }

    when {
        tailPosition.isBottom -> {
            // Start from top-left
            moveTo(cornerRadius, 0f)
            // Top edge
            lineTo(width - cornerRadius, 0f)
            // Top-right corner
            quadraticTo(width, 0f, width, cornerRadius)
            // Right edge
            lineTo(width, height - tailHeight - cornerRadius)
            // Bottom-right corner
            quadraticTo(width, height - tailHeight, width - cornerRadius, height - tailHeight)
            // Bottom edge (up to right side of tail)
            lineTo(tailCenterX + tailWidth / 2, height - tailHeight)
            // Tail
            lineTo(tailCenterX, height)
            lineTo(tailCenterX - tailWidth / 2, height - tailHeight)
            // Bottom edge (from left side of tail)
            lineTo(cornerRadius, height - tailHeight)
            // Bottom-left corner
            quadraticTo(0f, height - tailHeight, 0f, height - tailHeight - cornerRadius)
            // Left edge
            lineTo(0f, cornerRadius)
            // Top-left corner
            quadraticTo(0f, 0f, cornerRadius, 0f)
            close()
        }
        tailPosition.isTop -> {
            // Start from top-left (offset by tail height)
            moveTo(cornerRadius, tailHeight)
            // Top edge (up to left side of tail)
            lineTo(tailCenterX - tailWidth / 2, tailHeight)
            // Tail
            lineTo(tailCenterX, 0f)
            lineTo(tailCenterX + tailWidth / 2, tailHeight)
            // Top edge (from right side of tail)
            lineTo(width - cornerRadius, tailHeight)
            // Top-right corner
            quadraticTo(width, tailHeight, width, tailHeight + cornerRadius)
            // Right edge
            lineTo(width, height - cornerRadius)
            // Bottom-right corner
            quadraticTo(width, height, width - cornerRadius, height)
            // Bottom edge
            lineTo(cornerRadius, height)
            // Bottom-left corner
            quadraticTo(0f, height, 0f, height - cornerRadius)
            // Left edge
            lineTo(0f, tailHeight + cornerRadius)
            // Top-left corner
            quadraticTo(0f, tailHeight, cornerRadius, tailHeight)
            close()
        }
    }
}
