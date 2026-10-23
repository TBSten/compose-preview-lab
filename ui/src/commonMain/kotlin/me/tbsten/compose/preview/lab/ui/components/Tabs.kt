/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: PreviewLab* prefix, compose-unstyled 2.6.0, PreviewLabTheme facade colours.
 * No InteractionMode (tabs are always 36dp), labels use `typography.label1`.
 */
package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.ProvideTextStyle
import com.composeunstyled.Stack
import com.composeunstyled.StackOrientation
import com.composeunstyled.StackScope
import com.composeunstyled.UnstyledTab
import com.composeunstyled.UnstyledTabGroup
import com.composeunstyled.UnstyledTabList
import com.composeunstyled.UnstyledTabPanel
import com.composeunstyled.buildModifier
import kotlin.jvm.JvmInline
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

/** Receiver scope of [PreviewLabTabs]; hosts [PreviewLabTabList] and [PreviewLabTabPanel]. */
@UiComposePreviewLabApi
class PreviewLabTabsScope<T> internal constructor(internal val selectedTab: T, internal val orderedTabs: List<T>,)

/**
 * An individual tab inside a [PreviewLabTabList].
 *
 * @param key Key represented by this tab or tab panel.
 * @param modifier Modifier applied to the component.
 * @param enabled Whether the tab can be interacted with.
 * @param activateOnFocus Whether focusing the tab should immediately select it.
 * @param interactionSource Interaction source used for focus, hover, and press state.
 * @param indication Indication used when the tab is pressed or focused.
 * @param icon Optional icon content shown before the tab text.
 * @param text Text content shown inside the tab.
 */
@Composable
@UiComposePreviewLabApi
fun <T> PreviewLabTab(
    key: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activateOnFocus: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = LocalIndication.current,
    icon: (@Composable () -> Unit)? = null,
    text: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(4.dp)
    val updateTabIndicatorBounds = LocalTabIndicatorBoundsUpdater.current
    UnstyledTab(
        key = key,
        enabled = enabled,
        activateOnFocus = activateOnFocus,
        interactionSource = interactionSource,
        indication = indication,
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(
                buildModifier {
                    if (updateTabIndicatorBounds != null) {
                        add(Modifier.onGloballyPositioned { updateTabIndicatorBounds(key, it) })
                    }
                    if (!enabled) add(Modifier.alpha(DisabledAlpha))
                },
            )
            .bouncyPress(
                interactionSource = interactionSource,
                enabled = enabled,
            )
            .previewLabFocusRing(
                interactionSource = interactionSource,
                shape = shape,
            )
            .clip(shape),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = TabMinHeight)
                .padding(horizontal = TabContentHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideContentColor(
                if (selected) PreviewLabTheme.colors.primary else PreviewLabTheme.colors.muted,
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier.size(TabIconSize),
                        contentAlignment = Alignment.Center,
                    ) {
                        icon()
                    }
                    Box(Modifier.width(TabIconSpacing))
                }
                ProvideTextStyle(
                    LocalTextStyle.current.merge(PreviewLabTheme.typography.label1).merge(TabTextStyle),
                ) {
                    text()
                }
            }
        }
    }
}

/** Orientation options for arranging tab lists. */
@UiComposePreviewLabApi
@JvmInline
value class PreviewLabTabOrientation internal constructor(internal val orientation: Orientation) {
    @UiComposePreviewLabApi
    companion object {
        /** Arranges tabs in a horizontal row. */
        val Horizontal = PreviewLabTabOrientation(Orientation.Horizontal)

        /** Arranges tabs in a vertical column. */
        val Vertical = PreviewLabTabOrientation(Orientation.Vertical)
    }
}

/**
 * Tabs that coordinate tab triggers and panels.
 *
 * @param selectedTab Currently selected tab key.
 * @param onSelectedTabChange Called when the selected tab changes.
 * @param orderedTabs Ordered list of tab keys used for indicator placement and navigation.
 * @param modifier Modifier applied to the component.
 * @param content Composable content displayed by the component.
 */
@Composable
@UiComposePreviewLabApi
fun <T> PreviewLabTabs(
    selectedTab: T,
    onSelectedTabChange: (T) -> Unit,
    orderedTabs: List<T>,
    modifier: Modifier = Modifier,
    content: @Composable PreviewLabTabsScope<T>.() -> Unit,
) {
    UnstyledTabGroup(
        selectedTab = selectedTab,
        onSelectedTabChange = onSelectedTabChange,
        tabs = orderedTabs,
        modifier = modifier,
    ) {
        PreviewLabTabsScope(
            selectedTab = selectedTab,
            orderedTabs = orderedTabs,
        ).content()
    }
}

/**
 * A container that arranges tab triggers and draws the active indicator.
 *
 * The indicator bounds are measured relative to the list container and the indicator line is as wide as the
 * list content, so a `Modifier.horizontalScroll` on [modifier] scrolls the tabs and the indicator together.
 *
 * @param modifier Modifier applied to the component.
 * @param orientation Orientation used to lay out the tab list.
 * @param dividerColor Color used for the indicator divider line.
 * @param content Composable content displayed by the component.
 */
@Composable
@UiComposePreviewLabApi
fun <T> PreviewLabTabsScope<T>.PreviewLabTabList(
    modifier: Modifier = Modifier,
    orientation: PreviewLabTabOrientation = PreviewLabTabOrientation.Horizontal,
    dividerColor: Color = PreviewLabTheme.colors.outline,
    content: @Composable StackScope.() -> Unit,
) {
    val tabIndicatorBoundsByKey = remember { mutableStateMapOf<Any?, TabIndicatorBounds>() }
    var indicatorContainerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var indicatorContainerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val updateTabIndicatorBounds: (Any?, LayoutCoordinates) -> Unit = { key, tabCoordinates ->
        val containerCoordinates = indicatorContainerCoordinates
        if (containerCoordinates != null &&
            containerCoordinates.isAttached &&
            tabCoordinates.isAttached
        ) {
            if (orderedTabs.contains(key)) {
                val tabPosition = containerCoordinates.localPositionOf(tabCoordinates, Offset.Zero)
                tabIndicatorBoundsByKey[key] = with(density) {
                    if (orientation == PreviewLabTabOrientation.Horizontal) {
                        TabIndicatorBounds(
                            start = tabPosition.x.toDp(),
                            size = tabCoordinates.size.width.toDp(),
                        )
                    } else {
                        TabIndicatorBounds(
                            start = tabPosition.y.toDp(),
                            size = tabCoordinates.size.height.toDp(),
                        )
                    }
                }
            }
        }
    }
    val indicatorBounds = resolveTabIndicatorBounds(
        selectedTab = selectedTab,
        tabBoundsByKey = tabIndicatorBoundsByKey,
    )
    val tabListWidth = with(density) { indicatorContainerSize.width.toDp() }
    val tabListHeight = with(density) { indicatorContainerSize.height.toDp() }

    if (orientation == PreviewLabTabOrientation.Horizontal) {
        Column(modifier = modifier) {
            Box(
                modifier = Modifier.onGloballyPositioned {
                    indicatorContainerCoordinates = it
                    indicatorContainerSize = it.size
                },
            ) {
                UnstyledTabList(
                    orientation = orientation.orientation,
                ) {
                    Stack(orientation = orientation.toStackOrientation()) {
                        TabIndicatorBoundsProvider(updateTabIndicatorBounds) { content() }
                    }
                }
            }
            Spacer(Modifier.height(TabListBottomPadding))
            TabIndicatorLine(
                modifier = Modifier.width(tabListWidth),
                bounds = indicatorBounds,
                dividerColor = dividerColor,
            )
        }
    } else {
        Row(modifier = modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.onGloballyPositioned {
                    indicatorContainerCoordinates = it
                    indicatorContainerSize = it.size
                },
            ) {
                UnstyledTabList(
                    orientation = orientation.orientation,
                ) {
                    Stack(orientation = orientation.toStackOrientation()) {
                        TabIndicatorBoundsProvider(updateTabIndicatorBounds) { content() }
                    }
                }
            }
            Spacer(Modifier.width(TabListBottomPadding))
            VerticalTabIndicatorLine(
                modifier = Modifier.height(tabListHeight),
                bounds = indicatorBounds,
                dividerColor = dividerColor,
            )
        }
    }
}

/**
 * Content associated with a specific tab key; composed only while [key] is the selected tab.
 *
 * @param key Key represented by this tab or tab panel.
 * @param modifier Modifier applied to the component.
 * @param content Composable content displayed by the component.
 */
@Composable
@UiComposePreviewLabApi
fun <T> PreviewLabTabsScope<T>.PreviewLabTabPanel(key: T, modifier: Modifier = Modifier, content: @Composable () -> Unit,) {
    UnstyledTabPanel(key = key, modifier = modifier) { content() }
}

private fun PreviewLabTabOrientation.toStackOrientation(): StackOrientation = if (this == PreviewLabTabOrientation.Horizontal) {
    StackOrientation.Horizontal
} else {
    StackOrientation.Vertical
}

private val LocalTabIndicatorBoundsUpdater = compositionLocalOf<((Any?, LayoutCoordinates) -> Unit)?> { null }

@Composable
private fun TabIndicatorBoundsProvider(
    updateTabIndicatorBounds: ((Any?, LayoutCoordinates) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalTabIndicatorBoundsUpdater provides updateTabIndicatorBounds,
    ) {
        content()
    }
}

@Composable
private fun TabIndicatorLine(modifier: Modifier = Modifier, bounds: TabIndicatorBounds?, dividerColor: Color) {
    val placedBounds = placedTabIndicatorBounds(
        bounds = bounds,
        startLabel = "TabIndicatorStart",
        sizeLabel = "TabIndicatorWidth",
    )

    Box(
        modifier = modifier.height(TabIndicatorHeight).background(dividerColor, TabIndicatorShape),
    ) {
        if (bounds != null) {
            Box(
                modifier = Modifier
                    .offset(x = placedBounds.start)
                    .width(placedBounds.size)
                    .height(TabIndicatorHeight)
                    .background(
                        color = PreviewLabTheme.colors.primary,
                        shape = TabIndicatorShape,
                    ),
            )
        }
    }
}

@Composable
private fun VerticalTabIndicatorLine(modifier: Modifier = Modifier, bounds: TabIndicatorBounds?, dividerColor: Color) {
    val placedBounds = placedTabIndicatorBounds(
        bounds = bounds,
        startLabel = "VerticalTabIndicatorStart",
        sizeLabel = "VerticalTabIndicatorHeight",
    )

    Box(
        modifier = modifier.width(TabIndicatorHeight).background(dividerColor, TabIndicatorShape),
    ) {
        if (bounds != null) {
            Box(
                modifier = Modifier
                    .offset(y = placedBounds.start)
                    .width(TabIndicatorHeight)
                    .height(placedBounds.size)
                    .background(
                        color = PreviewLabTheme.colors.primary,
                        shape = TabIndicatorShape,
                    ),
            )
        }
    }
}

@Composable
private fun placedTabIndicatorBounds(bounds: TabIndicatorBounds?, startLabel: String, sizeLabel: String,): TabIndicatorBounds {
    var initialBoundsPlaced by remember { mutableStateOf(false) }
    if (bounds == null) {
        SideEffect { initialBoundsPlaced = false }
        return TabIndicatorBounds(start = 0.dp, size = 0.dp)
    }

    if (!initialBoundsPlaced) {
        SideEffect { initialBoundsPlaced = true }
        return bounds
    }

    return TabIndicatorBounds(
        start = animateTabIndicatorDp(bounds.start, startLabel),
        size = animateTabIndicatorDp(bounds.size, sizeLabel),
    )
}

@Composable
private fun animateTabIndicatorDp(targetValue: Dp, label: String): Dp {
    val animatedValue by animateDpAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 180),
        label = label,
    )
    return animatedValue
}

private fun resolveTabIndicatorBounds(selectedTab: Any?, tabBoundsByKey: Map<Any?, TabIndicatorBounds>,): TabIndicatorBounds? =
    tabBoundsByKey[selectedTab]

private data class TabIndicatorBounds(val start: Dp, val size: Dp,)

private val TabTextStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
)

/** Composables UI `disabledAlpha`. */
private const val DisabledAlpha = 0.33f

private val TabContentHorizontalPadding = 12.dp
private val TabIconSize = 24.dp
private val TabIconSpacing = 8.dp
private val TabIndicatorHeight = 1.dp
private val TabIndicatorShape = RoundedCornerShape(100)
private val TabListBottomPadding = 2.dp

/** Pointer-mode tab height (no InteractionMode in PreviewLab). */
private val TabMinHeight = 36.dp
