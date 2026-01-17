package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_close
import me.tbsten.compose.preview.lab.ui.util.AutoScrollToTopEffect
import org.jetbrains.compose.resources.painterResource

/**
 * A reusable BackStack list component for navigation fields.
 *
 * @param T The type of items in the backstack
 * @param backStack The list of backstack items (in original order, newest last)
 * @param canPop Whether the backstack can be popped
 * @param onPopBack Callback when pop back is requested
 * @param displayItem Function to convert an item to display text
 * @param itemKey Function to generate a unique key for each item
 * @param modifier Modifier for the component
 */
@InternalComposePreviewLabApi
@Composable
fun <T : Any> BackStackList(
    backStack: List<T>,
    canPop: Boolean,
    onPopBack: () -> Unit,
    displayItem: (T) -> String,
    itemKey: (index: Int, item: T) -> Any,
    modifier: Modifier = Modifier,
) {
    val reversedBackStack = backStack.reversed()
    val listState = rememberLazyListState()

    AutoScrollToTopEffect(listState, backStack.size)

    Box(modifier = modifier) {
        val bottomShadowHeight = 20.dp

        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Start),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = bottomShadowHeight),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(
                items = reversedBackStack,
                key = { index, item -> itemKey(backStack.size - 1 - index, item) },
            ) { index, item ->
                val isCurrent = index == 0
                BackStackItem(
                    position = reversedBackStack.size - index,
                    displayText = displayItem(item),
                    isCurrent = isCurrent,
                    canPop = canPop && isCurrent,
                    onPopBack = onPopBack,
                    modifier = Modifier.animateItem(),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomShadowHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            PreviewLabTheme.colors.background,
                        ),
                    ),
                ),
        )
    }
}

@InternalComposePreviewLabApi
@Composable
fun BackStackItem(
    position: Int,
    displayText: String,
    isCurrent: Boolean,
    canPop: Boolean,
    onPopBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = if (isCurrent) {
            PreviewLabTheme.colors.primary.copy(alpha = 0.1f)
        } else {
            PreviewLabTheme.colors.surface
        },
        border = if (isCurrent) {
            BorderStroke(1.dp, PreviewLabTheme.colors.primary)
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = isCurrent,
                enter = fadeIn() + expandHorizontally(clip = false),
                exit = fadeOut() + shrinkHorizontally(clip = false),
            ) {
                Row {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = PreviewLabTheme.colors.primary,
                        modifier = Modifier.size(6.dp),
                    ) {}
                    Spacer(Modifier.width(6.dp))
                }
            }
            Text(
                text = "$position. $displayText",
                style = PreviewLabTheme.typography.body2,
                minLines = 1,
                maxLines = 3,
                overflow = TextOverflow.MiddleEllipsis,
                color = if (isCurrent) {
                    PreviewLabTheme.colors.primary
                } else {
                    PreviewLabTheme.colors.onSurface
                },
                modifier = Modifier.weight(1f),
            )
            AnimatedVisibility(
                visible = isCurrent && canPop,
                enter = fadeIn() + expandHorizontally(clip = false),
                exit = fadeOut() + shrinkHorizontally(clip = false),
            ) {
                IconButton(
                    onClick = onPopBack,
                    variant = IconButtonVariant.Ghost,
                    modifier = Modifier.size(24.dp),
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(PreviewLabUiRes.drawable.icon_close),
                        contentDescription = "Pop back",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
