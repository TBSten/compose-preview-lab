/*
 * Copyright (c) 2026 Composable Horizons — MIT License (see ui/THIRD_PARTY_NOTICES.md).
 * Adapted for Compose Preview Lab: `PreviewLabThemeComposable` derives every compose-unstyled token from the
 * PreviewLab `Colors`/`Typography` facade instead of hard-coded light/dark values; no InteractionMode / ColorScheme.
 */
package me.tbsten.compose.preview.lab.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.composables.compose.ripple.rememberRippleIndication
import com.composeunstyled.FocusVisibilityProvider
import com.composeunstyled.TooltipHost
import com.composeunstyled.theme.ThemeComposable
import com.composeunstyled.theme.buildTheme
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButtonDefaults
import me.tbsten.compose.preview.lab.ui.components.textfield.PreviewLabTextFieldDefaults

/** Facade over the current PreviewLab palette and type scale. Unchanged API. */
@UiComposePreviewLabApi
object PreviewLabTheme {
    val colors: Colors
        @ReadOnlyComposable @Composable
        get() = LocalColors.current

    val typography: Typography
        @ReadOnlyComposable @Composable
        get() = LocalTypography.current
}

/** True below the outermost [PreviewLabTheme]; nested themes must not install a second (clipped) TooltipHost. */
internal val LocalIsInsidePreviewLabThemeHosts = staticCompositionLocalOf { false }

/**
 * PreviewLab theme root (signature unchanged: PreviewLab.kt, PreviewLabGallery.kt, dev tests).
 *
 * Provides [LocalColors]/[LocalTypography] (facade) and, through compose-unstyled `buildTheme`, `LocalTheme`,
 * `LocalIndication` (ripple), `LocalTextStyle` (= body1), `LocalContentColor` (= onBackground) and
 * `LocalTextSelectionColors`; plus a `TooltipHost` (Composables tooltips render through a Portal) and a
 * `FocusVisibilityProvider` (keyboard focus rings). The hosts are installed by the outermost theme only.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabTheme(isDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val typography = provideTypography()
    val installHosts = !LocalIsInsidePreviewLabThemeHosts.current
    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides typography,
        LocalIsInsidePreviewLabThemeHosts provides true,
    ) {
        PreviewLabThemeComposable {
            if (installHosts) {
                TooltipHost {
                    FocusVisibilityProvider {
                        content()
                    }
                }
            } else {
                content()
            }
        }
    }
}

/**
 * compose-unstyled 2.6.0 theme (no `extend {}` in 2.6.0; nothing else has to be provided from inside).
 * The builder lambda is @Composable and runs inside the CompositionLocalProvider above, so every token is derived
 * from the same [Colors]/[Typography] instance (single source of truth = LightColors/DarkColors + defaultTypography).
 */
internal val PreviewLabThemeComposable: ThemeComposable = buildTheme {
    val palette = LocalColors.current // deliberately not named `colors` (would shadow the token property)
    val typography = LocalTypography.current

    name = "PreviewLabTheme"

    properties[colors] = mapOf(
        backgroundColor to palette.background,
        onBackgroundColor to palette.onBackground,
        panelColor to palette.panel,
        onPanelColor to palette.onPanel,
        mutedColor to palette.muted,
        primaryColor to palette.primary,
        onPrimaryColor to palette.onPrimary,
        secondaryColor to palette.secondary,
        onSecondaryColor to palette.onSecondary,
        controlColor to palette.control,
        onControlColor to palette.onControl,
        thumbColor to palette.thumb,
        switchTrackColor to palette.switchTrack,
        switchSelectedTrackColor to palette.switchSelectedTrack,
        switchThumbColor to palette.switchThumb,
        selectedControlColor to palette.selectedControl,
        onSelectedControlColor to palette.onSelectedControl,
        destructiveColor to palette.error,
        onDestructiveColor to palette.onError,
        borderColor to palette.outline,
        fieldColor to palette.field,
        onFieldColor to palette.onField,
        scrimColor to palette.scrim,
        ringColor to palette.ring,
        successColor to palette.success,
        onSuccessColor to palette.onSuccess,
        infoColor to palette.tertiary,
        onInfoColor to palette.onTertiary,
    )
    properties[textStyles] = mapOf(
        h1TextStyle to typography.h1,
        h2TextStyle to typography.h2,
        h3TextStyle to typography.h3,
        h4TextStyle to typography.h4,
        body1TextStyle to typography.body1,
        body2TextStyle to typography.body2,
        body3TextStyle to typography.body3,
        label1TextStyle to typography.label1,
        label2TextStyle to typography.label2,
        label3TextStyle to typography.label3,
        buttonTextStyle to typography.button,
        inputTextStyle to typography.input,
    )
    properties[shapes] = mapOf(
        smallShape to RoundedCornerShape(6.dp),
        mediumShape to RoundedCornerShape(12.dp),
        largeShape to RoundedCornerShape(16.dp),
        buttonShape to PreviewLabButtonDefaults.ButtonShape, // RoundedCornerShape(10.dp) — Composables pointer value
        dialogShape to RoundedCornerShape(16.dp),
        sheetShape to RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        menuShape to RoundedCornerShape(16.dp),
        fieldShape to PreviewLabTextFieldDefaults.Shape, // RoundedCornerShape(12.dp) — Composables pointer value
    )
    properties[shadows] = mapOf(
        raisedShadow to Shadow(
            radius = 16.dp,
            color = Color.Black,
            offset = DpOffset(0.dp, 4.dp),
            alpha = 0.14f,
        ),
        overlayShadow to Shadow(
            radius = 24.dp,
            color = Color.Black,
            offset = DpOffset(0.dp, 8.dp),
            alpha = if (palette.isDark) 0.36f else 0.22f,
        ),
    )
    properties[alphas] = mapOf(disabledAlpha to 0.33f)

    val rippleDefault = rememberRippleIndication(palette.defaultRippleColor)
    val rippleInverse = rememberRippleIndication(palette.inverseRippleColor)
    properties[indications] = mapOf(
        // FQN: the ThemeBuilder member `defaultIndication` shadows the token of the same name
        me.tbsten.compose.preview.lab.ui.defaultIndication to rippleDefault,
        inverseIndication to rippleInverse,
    )

    val selection = TextSelectionColors(
        handleColor = palette.primary,
        backgroundColor = palette.primary.copy(alpha = 0.24f),
    )
    properties[textSelectionColors] = mapOf(
        // FQN: the ThemeBuilder member `defaultTextSelectionColors` shadows the token of the same name
        me.tbsten.compose.preview.lab.ui.defaultTextSelectionColors to selection,
    )

    // ThemeBuilder defaults → unstyled provides LocalIndication / LocalTextStyle / LocalContentColor /
    // LocalTextSelectionColors from these.
    defaultIndication = rippleDefault // otherwise buildTheme resets LocalIndication to NoIndication
    defaultTextStyle = typography.body1 // == the previous LocalTextStyle default
    defaultContentColor = palette.onBackground // == contentColorFor(background); unstyled default is Unspecified
    defaultTextSelectionColors = selection // selection colours now follow dark mode
}

/**
 * Content colour for [color], falling back to the surrounding `LocalContentColor` when [color] is not a palette
 * colour (Transparent, alpha tints, animated colours) — Material behaviour. Surface/Card defaults use this.
 */
@Composable
@ReadOnlyComposable
@UiComposePreviewLabApi
fun contentColorFor(color: Color): Color =
    PreviewLabTheme.colors.contentColorFor(color).takeOrElse { com.composeunstyled.LocalContentColor.current }

// ---- deprecated shims for downstream @UiComposePreviewLabApi users (ui code imports com.composeunstyled.* directly) ----

@Deprecated(
    "Use com.composeunstyled.LocalContentColor",
    ReplaceWith("LocalContentColor", "com.composeunstyled.LocalContentColor"),
)
@UiComposePreviewLabApi
val LocalContentColor: ProvidableCompositionLocal<Color>
    get() = com.composeunstyled.LocalContentColor

@Deprecated(
    "Use com.composeunstyled.LocalTextStyle",
    ReplaceWith("LocalTextStyle", "com.composeunstyled.LocalTextStyle"),
)
@UiComposePreviewLabApi
val LocalTextStyle: ProvidableCompositionLocal<TextStyle>
    get() = com.composeunstyled.LocalTextStyle
