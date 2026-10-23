/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: package renamed, PreviewLab-only tokens (success/info/textStyles) added.
 */
package me.tbsten.compose.preview.lab.ui

import androidx.compose.foundation.Indication
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import com.composeunstyled.theme.ThemeProperty
import com.composeunstyled.theme.ThemeToken
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

// ---- colors (Composables UI names, verbatim) ----

/** Theme property that contains the colour tokens. Values are populated from [Colors] by [PreviewLabTheme]. */
@UiComposePreviewLabApi val colors: ThemeProperty<Color> = ThemeProperty("colors")

/** Background color of a given screen. */
@UiComposePreviewLabApi val backgroundColor: ThemeToken<Color> = ThemeToken("background")

/** Content color that sits directly on [backgroundColor]. */
@UiComposePreviewLabApi val onBackgroundColor: ThemeToken<Color> = ThemeToken("on_background")

/** Background color for components such as panels, cards, dialogs, sheets, and menus. */
@UiComposePreviewLabApi val panelColor: ThemeToken<Color> = ThemeToken("panel")

/** Content color that sits directly on [panelColor]. */
@UiComposePreviewLabApi val onPanelColor: ThemeToken<Color> = ThemeToken("on_panel")

/** Lower-emphasis content color. */
@UiComposePreviewLabApi val mutedColor: ThemeToken<Color> = ThemeToken("muted")

/** Color used for primary actions. */
@UiComposePreviewLabApi val primaryColor: ThemeToken<Color> = ThemeToken("primary")

/** Content color that sits directly on [primaryColor]. */
@UiComposePreviewLabApi val onPrimaryColor: ThemeToken<Color> = ThemeToken("on_primary")

/** Color used for secondary actions. */
@UiComposePreviewLabApi val secondaryColor: ThemeToken<Color> = ThemeToken("secondary")

/** Content color that sits directly on [secondaryColor]. */
@UiComposePreviewLabApi val onSecondaryColor: ThemeToken<Color> = ThemeToken("on_secondary")

/** Background color for controls, such as a progress bar track. */
@UiComposePreviewLabApi val controlColor: ThemeToken<Color> = ThemeToken("control")

/** Content color that sits directly on [controlColor]. */
@UiComposePreviewLabApi val onControlColor: ThemeToken<Color> = ThemeToken("on_control")

/** Thumb color for controls such as sliders and toggles. */
@UiComposePreviewLabApi val thumbColor: ThemeToken<Color> = ThemeToken("thumb_color")

/** Default switch track color. */
@UiComposePreviewLabApi val switchTrackColor: ThemeToken<Color> = ThemeToken("switch_track_color")

/** Selected switch track color. */
@UiComposePreviewLabApi val switchSelectedTrackColor: ThemeToken<Color> = ThemeToken("switch_selected_track_color")

/** Switch thumb color. */
@UiComposePreviewLabApi val switchThumbColor: ThemeToken<Color> = ThemeToken("switch_thumb_color")

/** Background color for selected controls. */
@UiComposePreviewLabApi val selectedControlColor: ThemeToken<Color> = ThemeToken("selected_control")

/** Content color that sits directly on [selectedControlColor]. */
@UiComposePreviewLabApi val onSelectedControlColor: ThemeToken<Color> = ThemeToken("on_selected_control")

/** Color used for destructive actions such as deletion. */
@UiComposePreviewLabApi val destructiveColor: ThemeToken<Color> = ThemeToken("destructive")

/** Content color that sits directly on [destructiveColor]. */
@UiComposePreviewLabApi val onDestructiveColor: ThemeToken<Color> = ThemeToken("on_destructive")

/** Border and separator color. */
@UiComposePreviewLabApi val borderColor: ThemeToken<Color> = ThemeToken("border")

/** Background color used for input fields such as text fields. */
@UiComposePreviewLabApi val fieldColor: ThemeToken<Color> = ThemeToken("field")

/** Content color that sits directly on [fieldColor]. */
@UiComposePreviewLabApi val onFieldColor: ThemeToken<Color> = ThemeToken("on_field")

/** Scrim color behind modal content. */
@UiComposePreviewLabApi val scrimColor: ThemeToken<Color> = ThemeToken("scrim")

/** Focus ring color. */
@UiComposePreviewLabApi val ringColor: ThemeToken<Color> = ThemeToken("ring")

// ---- PreviewLab additions (no Composables counterpart; Toast Success / Info) ----

/** Color used for success states (PreviewLab addition). */
@UiComposePreviewLabApi val successColor: ThemeToken<Color> = ThemeToken("success")

/** Content color that sits directly on [successColor] (PreviewLab addition). */
@UiComposePreviewLabApi val onSuccessColor: ThemeToken<Color> = ThemeToken("on_success")

/** Color used for informational states (PreviewLab addition; maps to [Colors.tertiary]). */
@UiComposePreviewLabApi val infoColor: ThemeToken<Color> = ThemeToken("info")

/** Content color that sits directly on [infoColor] (PreviewLab addition). */
@UiComposePreviewLabApi val onInfoColor: ThemeToken<Color> = ThemeToken("on_info")

// ---- shapes ----

/** Theme property that contains the shape tokens. */
@UiComposePreviewLabApi val shapes: ThemeProperty<Shape> = ThemeProperty("shapes")

/** Small shape used by compact controls. */
@UiComposePreviewLabApi val smallShape: ThemeToken<Shape> = ThemeToken("small_shape")

/** Medium shape used by medium-sized containers. */
@UiComposePreviewLabApi val mediumShape: ThemeToken<Shape> = ThemeToken("medium_shape")

/** Large shape used by larger surfaces. */
@UiComposePreviewLabApi val largeShape: ThemeToken<Shape> = ThemeToken("large_shape")

/** Shape for buttons. */
@UiComposePreviewLabApi val buttonShape: ThemeToken<Shape> = ThemeToken("button_shape")

/** Shape for dialogs. */
@UiComposePreviewLabApi val dialogShape: ThemeToken<Shape> = ThemeToken("dialog_shape")

/** Shape for bottom sheets. */
@UiComposePreviewLabApi val sheetShape: ThemeToken<Shape> = ThemeToken("sheet_shape")

/** Shape for menus. */
@UiComposePreviewLabApi val menuShape: ThemeToken<Shape> = ThemeToken("menu_shape")

/** Shape for text fields. */
@UiComposePreviewLabApi val fieldShape: ThemeToken<Shape> = ThemeToken("field_shape")

// ---- shadows / indications / alphas / selection ----

/** Theme property that contains the shadow tokens. */
@UiComposePreviewLabApi val shadows: ThemeProperty<Shadow> = ThemeProperty("shadows")

/** Shadow for raised controls and surfaces. */
@UiComposePreviewLabApi val raisedShadow: ThemeToken<Shadow> = ThemeToken("raised_shadow")

/** Shadow for overlay surfaces such as dialogs, sheets, and menus. */
@UiComposePreviewLabApi val overlayShadow: ThemeToken<Shadow> = ThemeToken("overlay_shadow")

/** Theme property that contains the indication tokens. */
@UiComposePreviewLabApi val indications: ThemeProperty<Indication> = ThemeProperty("indications")

/**
 * Indication for controls.
 *
 * Inside `buildTheme {}` the `ThemeBuilder.defaultIndication` member shadows this token; refer to it there by its
 * fully qualified name (`me.tbsten.compose.preview.lab.ui.defaultIndication`).
 */
@UiComposePreviewLabApi val defaultIndication: ThemeToken<Indication> = ThemeToken("default_indication")

/** Ripple indication for content on inverse or high-contrast surfaces. */
@UiComposePreviewLabApi val inverseIndication: ThemeToken<Indication> = ThemeToken("inverse_indication")

/** Theme property that contains alpha tokens. */
@UiComposePreviewLabApi val alphas: ThemeProperty<Float> = ThemeProperty("alphas")

/** Alpha value applied to disabled components. */
@UiComposePreviewLabApi val disabledAlpha: ThemeToken<Float> = ThemeToken("disabled_alpha")

/** Theme property that contains text selection color tokens. */
@UiComposePreviewLabApi val textSelectionColors: ThemeProperty<TextSelectionColors> =
    ThemeProperty("text_selection_colors")

/**
 * Default handle and background colors for text selection.
 *
 * Inside `buildTheme {}` the `ThemeBuilder.defaultTextSelectionColors` member shadows this token; refer to it there by
 * its fully qualified name (`me.tbsten.compose.preview.lab.ui.defaultTextSelectionColors`).
 */
@UiComposePreviewLabApi val defaultTextSelectionColors: ThemeToken<TextSelectionColors> =
    ThemeToken("default_text_selection_colors")

// ---- PreviewLab addition: the typography scale as tokens (Composables UI has only defaultTextStyle) ----

/** Theme property that contains the [Typography] scale as tokens (PreviewLab addition). */
@UiComposePreviewLabApi val textStyles: ThemeProperty<TextStyle> = ThemeProperty("text_styles")

/** [Typography.h1] */
@UiComposePreviewLabApi val h1TextStyle: ThemeToken<TextStyle> = ThemeToken("h1")

/** [Typography.h2] */
@UiComposePreviewLabApi val h2TextStyle: ThemeToken<TextStyle> = ThemeToken("h2")

/** [Typography.h3] */
@UiComposePreviewLabApi val h3TextStyle: ThemeToken<TextStyle> = ThemeToken("h3")

/** [Typography.h4] */
@UiComposePreviewLabApi val h4TextStyle: ThemeToken<TextStyle> = ThemeToken("h4")

/** [Typography.body1] */
@UiComposePreviewLabApi val body1TextStyle: ThemeToken<TextStyle> = ThemeToken("body1")

/** [Typography.body2] */
@UiComposePreviewLabApi val body2TextStyle: ThemeToken<TextStyle> = ThemeToken("body2")

/** [Typography.body3] */
@UiComposePreviewLabApi val body3TextStyle: ThemeToken<TextStyle> = ThemeToken("body3")

/** [Typography.label1] */
@UiComposePreviewLabApi val label1TextStyle: ThemeToken<TextStyle> = ThemeToken("label1")

/** [Typography.label2] */
@UiComposePreviewLabApi val label2TextStyle: ThemeToken<TextStyle> = ThemeToken("label2")

/** [Typography.label3] */
@UiComposePreviewLabApi val label3TextStyle: ThemeToken<TextStyle> = ThemeToken("label3")

/** [Typography.button] */
@UiComposePreviewLabApi val buttonTextStyle: ThemeToken<TextStyle> = ThemeToken("button")

/** [Typography.input] */
@UiComposePreviewLabApi val inputTextStyle: ThemeToken<TextStyle> = ThemeToken("input")
