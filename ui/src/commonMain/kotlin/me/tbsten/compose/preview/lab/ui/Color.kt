/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: token values from the Composables UI theme exposed through the PreviewLab
 * `Colors` facade, PreviewLab-only success/info/text colours added.
 */
package me.tbsten.compose.preview.lab.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

/**
 * PreviewLab colour facade. Values are the Composables UI theme tokens (light/dark) plus PreviewLab-only
 * success/info colours. Read through [PreviewLabTheme.colors]; the same instance feeds the compose-unstyled tokens.
 */
@Immutable
@UiComposePreviewLabApi
data class Colors(
    val isDark: Boolean,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val onSuccess: Color,
    val disabled: Color,
    val onDisabled: Color,
    val surface: Color,
    val onSurface: Color,
    val background: Color,
    val onBackground: Color,
    val outline: Color,
    val outlineSecondary: Color,
    val transparent: Color = Color.Transparent,
    val white: Color = Color.White,
    val black: Color = Color.Black,
    val text: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    /** Scrim colour for modal overlays (currently unused: PreviewLabModal relies on the platform Dialog scrim). */
    val scrim: Color,
    val elevation: Color,
    // Composables UI tokens without a Lumo counterpart
    val panel: Color,
    val onPanel: Color,
    val muted: Color,
    val control: Color,
    val onControl: Color,
    val selectedControl: Color,
    val onSelectedControl: Color,
    val field: Color,
    val onField: Color,
    val ring: Color,
    val thumb: Color,
    val switchTrack: Color,
    val switchSelectedTrack: Color,
    val switchThumb: Color,
)

@UiComposePreviewLabApi
val LightColors: Colors = Colors(
    isDark = false,
    primary = Color(0xFF000000),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFF1F1F1),
    onSecondary = Color(0xFF000000),
    tertiary = Color(0xFF2563EB),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    success = Color(0xFF16A34A),
    onSuccess = Color(0xFFFFFFFF),
    disabled = Color(0xFFE5E5E5),
    onDisabled = Color(0xFF777777),
    surface = Color(0xFFE5E5E5),
    onSurface = Color(0xFF000000),
    background = Color(0xFFF4F4F4),
    onBackground = Color(0xFF000000),
    outline = Color(0xFFE0E0E0),
    outlineSecondary = Color(0xFFDCDCDC),
    text = Color(0xFF000000),
    textSecondary = Color(0xFF777777),
    textDisabled = Color(0xFFB3B3B3),
    scrim = Color.Black.copy(alpha = 0.38f),
    elevation = Color.Black,
    panel = Color(0xFFFFFFFF),
    onPanel = Color(0xFF000000),
    muted = Color(0xFF777777),
    control = Color(0xFFE5E5E5),
    onControl = Color(0xFF777777),
    selectedControl = Color(0xFFECECEC),
    onSelectedControl = Color(0xFF000000),
    field = Color(0xFFF1F1F1),
    onField = Color(0xFF000000),
    ring = Color.Black.copy(alpha = 0.24f),
    thumb = Color(0xFFFFFFFF),
    switchTrack = Color(0xFFE4E4E7),
    switchSelectedTrack = Color(0xFF18181B),
    switchThumb = Color(0xFFFFFFFF),
)

@UiComposePreviewLabApi
val DarkColors: Colors = Colors(
    isDark = true,
    primary = Color(0xFFF5F5F5),
    onPrimary = Color(0xFF171717),
    secondary = Color(0xFF262626),
    onSecondary = Color(0xFFE5E5E5),
    tertiary = Color(0xFF60A5FA),
    onTertiary = Color(0xFF171717),
    error = Color(0xFFF87171),
    onError = Color(0xFF171717),
    success = Color(0xFF4ADE80),
    onSuccess = Color(0xFF171717),
    disabled = Color(0xFF262626),
    onDisabled = Color(0xFFA3A3A3),
    surface = Color(0xFF262626),
    onSurface = Color(0xFFE5E5E5),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFE5E5E5),
    outline = Color(0xFF404040),
    outlineSecondary = Color(0xFF2E2E2E),
    text = Color(0xFFE5E5E5),
    textSecondary = Color(0xFFA3A3A3),
    textDisabled = Color(0xFF6B6B6B),
    scrim = Color.Black.copy(alpha = 0.48f),
    elevation = Color.Black,
    panel = Color(0xFF171717),
    onPanel = Color(0xFFE5E5E5),
    muted = Color(0xFFA3A3A3),
    control = Color(0xFF262626),
    onControl = Color(0xFFA3A3A3),
    selectedControl = Color(0xFF404040),
    onSelectedControl = Color(0xFFF5F5F5),
    field = Color(0xFF262626),
    onField = Color(0xFFE5E5E5),
    ring = Color(0xFFE5E5E5).copy(alpha = 0.28f),
    thumb = Color(0xFFE5E5E5),
    switchTrack = Color.White.copy(alpha = 0.05f),
    switchSelectedTrack = Color.White.copy(alpha = 0.25f),
    switchThumb = Color(0xFFFFFFFF),
)

@UiComposePreviewLabApi
val LocalColors = staticCompositionLocalOf { LightColors }

/**
 * Content colour for a container painted with one of this palette's colours; [Color.Unspecified] otherwise
 * (callers fall back to the surrounding unstyled content colour — see the @Composable contentColorFor in Theme.kt).
 *
 * Branch ORDER matters: several tokens share a value (light disabled = surface = control; dark secondary =
 * disabled = surface = control = field). `when` takes the first match, so surface-like branches come first and
 * `disabled` (muted content) comes last.
 */
@UiComposePreviewLabApi
fun Colors.contentColorFor(backgroundColor: Color): Color = when (backgroundColor) {
    transparent -> Color.Unspecified // explicit: inherit the surrounding content colour
    background -> onBackground
    panel -> onPanel
    surface, control -> onSurface
    field -> onField
    secondary -> onSecondary
    selectedControl -> onSelectedControl
    primary -> onPrimary
    tertiary -> onTertiary
    error -> onError
    success -> onSuccess
    disabled -> onDisabled // last: same value as surface/control
    else -> Color.Unspecified
}

// ---- ripple colours used by the theme and by Primary/Destructive buttons (Composables values) ----
internal val Colors.defaultRippleColor: Color get() = onBackground.copy(alpha = if (isDark) 0.10f else 0.08f)
internal val Colors.inverseRippleColor: Color get() = onPrimary.copy(alpha = if (isDark) 0.10f else 0.12f)
