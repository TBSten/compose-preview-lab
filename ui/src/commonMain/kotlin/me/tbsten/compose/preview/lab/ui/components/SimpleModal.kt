package me.tbsten.compose.preview.lab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composeunstyled.FocusVisibilityProvider
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.TooltipHost
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_close
import org.jetbrains.compose.resources.painterResource

/**
 * Full-window modal built on [Dialog]. The panel is drawn Composables UI style (panel colour, 16dp corners, overlay
 * shadow); a close button sits above it (the only close affordance on iOS, which has neither Esc nor Back).
 * Tapping the scrim, Esc (desktop) and Back (Android) dismiss through [onDismissRequest].
 *
 * The compose-unstyled `TooltipHost` / `FocusVisibilityProvider` wrap the whole dialog content (a Portal would
 * otherwise render into the main window behind the scrim), so tooltips inside the modal may overlap the scrim.
 *
 * @param isVisible Whether the modal is shown.
 * @param onDismissRequest Called when the user asks to close the modal (scrim tap, close button, Esc / Back).
 * @param contentPadding Padding around the close button + panel column.
 * @param contentAlignment Alignment of the column inside the full-size scrim box.
 * @param content Panel content; it receives `onPanel` as content colour.
 */
@Composable
@UiComposePreviewLabApi
fun PreviewLabModal(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable () -> Unit,
) {
    if (!isVisible) return
    val colors = PreviewLabTheme.colors
    val panelShape = RoundedCornerShape(ModalPanelCornerRadius) // dialogShape
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Hosts wrap the WHOLE dialog content (not the clipped panel): tooltips may overlap the scrim and focus rings
        // keep working after clicks inside the dialog.
        TooltipHost(Modifier.fillMaxSize()) {
            FocusVisibilityProvider(Modifier.fillMaxSize()) {
                Box(
                    contentAlignment = contentAlignment,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures { onDismissRequest() } }, // scrim tap
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .padding(contentPadding)
                            .widthIn(min = ModalMinWidth, max = ModalMaxWidth),
                    ) {
                        // The only close affordance on iOS (no Esc, no Back); contentDescription "close" kept for tests.
                        // Unpainted column area still forwards taps to the scrim handler above.
                        PreviewLabIconButton(
                            painter = painterResource(PreviewLabUiRes.drawable.icon_close),
                            contentDescription = "close",
                            onClick = onDismissRequest,
                            style = PreviewLabButtonStyle.Ghost,
                            tint = Color.White,
                            tooltip = null,
                            modifier = Modifier.align(Alignment.End),
                        )
                        Box(
                            Modifier
                                .dropShadow(
                                    panelShape,
                                    // overlayShadow (see Theme.kt `properties[shadows]`)
                                    Shadow(
                                        radius = 24.dp,
                                        color = Color.Black,
                                        offset = DpOffset(0.dp, 8.dp),
                                        alpha = if (colors.isDark) 0.36f else 0.22f,
                                    ),
                                )
                                .clip(panelShape)
                                .background(colors.panel)
                                .disableTapEvents(), // swallow taps inside the panel
                        ) {
                            ProvideContentColor(colors.onPanel) { content() }
                        }
                    }
                }
            }
        }
    }
}

private val ModalPanelCornerRadius = 16.dp
private val ModalMinWidth = 280.dp
private val ModalMaxWidth = 600.dp

private fun Modifier.disableTapEvents() = pointerInput(Unit) { detectTapGestures { } }
