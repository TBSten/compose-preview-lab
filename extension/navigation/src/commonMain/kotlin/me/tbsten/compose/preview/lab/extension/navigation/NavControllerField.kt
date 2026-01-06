package me.tbsten.compose.preview.lab.extension.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.ImmutablePreviewLabField
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.field.PolymorphicField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.Checkbox
import me.tbsten.compose.preview.lab.ui.components.IconButton
import me.tbsten.compose.preview.lab.ui.components.IconButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Surface
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_close
import org.jetbrains.compose.resources.painterResource

/**
 * A PreviewLabField for inspecting and controlling a [NavHostController].
 *
 * This field provides:
 * - BackStack history display with animations
 * - Navigation controls (pop back, navigate to routes)
 * - Route selection with editable parameters via [PolymorphicField]
 *
 * @param label The label for this field
 * @param navController The NavHostController to inspect and control
 * @param routes List of route fields for navigation. Use [FixedField] for routes without
 *   parameters, and [combined] for routes with parameters.
 *
 * @see me.tbsten.compose.preview.lab.sample.extension.navigation.NavControllerFieldExample
 * For a complete usage example, see the integration test sample.
 */
@ExperimentalComposePreviewLabApi
class NavControllerField(
    label: String,
    private val navController: NavHostController,
    routes: List<PreviewLabField<out Any>> = emptyList(),
) : ImmutablePreviewLabField<NavHostController>(
    label = label,
    initialValue = navController,
) {
    private val routeField: PolymorphicField<Any>? = if (routes.isNotEmpty()) {
        @Suppress("UNCHECKED_CAST")
        PolymorphicField(
            label = "Route",
            initialValue = routes.first().value,
            fields = routes,
        )
    } else {
        null
    }

    override fun valueCode(): String = "rememberNavController()"
    override fun serializer() = null

    @OptIn(InternalComposePreviewLabApi::class)
    @Composable
    override fun Content() {
        val backStack by navController.currentBackStack.collectAsState()
        val canPop = backStack.size > 1

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SegmentedCard {
                SegmentedCardSection(isTop = true) {
                    Text(
                        text = "BackStack",
                        style = PreviewLabTheme.typography.label1,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    BackStackList(
                        backStack = backStack,
                        canPop = canPop,
                        onPopBack = { navController.popBackStack() },
                        modifier = Modifier.height(120.dp),
                    )
                }

                if (routeField != null) {
                    SegmentedCardSection(isTop = false) {
                        Text(
                            text = "navigate()",
                            style = PreviewLabTheme.typography.label1,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )

                        routeField.Content()

                        Spacer(Modifier.height(8.dp))

                        NavOptionsSection(
                            onNavigate = { options ->
                                navController.navigate(routeField.value, options)
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(InternalComposePreviewLabApi::class)
@Composable
private fun SegmentedCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PreviewLabTheme.colors.outline),
        color = Color.Transparent,
    ) {
        Column {
            content()
        }
    }
}

@OptIn(InternalComposePreviewLabApi::class)
@Composable
private fun SegmentedCardSection(isTop: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isTop) {
                    Modifier.padding(top = 1.dp)
                } else {
                    Modifier
                },
            ),
    ) {
        if (!isTop) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = PreviewLabTheme.colors.outline,
            ) {}
        }
        Box(
            modifier = Modifier.padding(12.dp),
        ) {
            Column {
                content()
            }
        }
    }
}

@OptIn(InternalComposePreviewLabApi::class)
@Composable
private fun NavOptionsSection(onNavigate: (androidx.navigation.NavOptions?) -> Unit) {
    var showOptions by remember { mutableStateOf(false) }
    var launchSingleTop by remember { mutableStateOf(false) }
    var restoreState by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (showOptions) 90f else 0f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Button(
                onClick = {
                    val options = if (launchSingleTop || restoreState) {
                        navOptions {
                            this.launchSingleTop = launchSingleTop
                            this.restoreState = restoreState
                        }
                    } else {
                        null
                    }
                    onNavigate(options)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Navigate")
            }
            Button(
                onClick = { showOptions = !showOptions },
            ) {
                Text(
                    text = ">",
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                )
            }
        }

        AnimatedVisibility(
            visible = showOptions,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = launchSingleTop,
                        onCheckedChange = { launchSingleTop = it },
                    )
                    Text(
                        text = "launchSingleTop",
                        style = PreviewLabTheme.typography.body2,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(
                        checked = restoreState,
                        onCheckedChange = { restoreState = it },
                    )
                    Text(
                        text = "restoreState",
                        style = PreviewLabTheme.typography.body2,
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoScrollToTopEffect(listState: LazyListState, key: Any) {
    LaunchedEffect(key) {
        val wasAtTop = listState.firstVisibleItemIndex == 0 &&
            listState.firstVisibleItemScrollOffset == 0
        if (wasAtTop) {
            listState.animateScrollToItem(0)
        }
    }
}

@Composable
private fun BackStackList(
    backStack: List<NavBackStackEntry>,
    canPop: Boolean,
    onPopBack: () -> Unit,
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
                key = { _, entry -> entry.id },
            ) { index, entry ->
                val isCurrent = entry == backStack.lastOrNull()
                BackStackItem(
                    position = reversedBackStack.size - index,
                    entry = entry,
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

@OptIn(InternalComposePreviewLabApi::class)
@Composable
private fun BackStackItem(
    position: Int,
    entry: NavBackStackEntry,
    isCurrent: Boolean,
    canPop: Boolean,
    onPopBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val routePattern = entry.destination.route?.substringAfterLast('.') ?: "(unnamed)"
    val displayText = entry.savedStateHandle.keys().fold(routePattern) { route, key ->
        val value = entry.savedStateHandle.get<Any>(key)?.toString() ?: key
        route.replace("{$key}", value)
    }

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
