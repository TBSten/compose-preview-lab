package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.tbsten.compose.preview.lab.extension.debugger.DebugMenu

/**
 * A dialog composable that displays the debug menu with built-in trigger support.
 *
 * This dialog automatically includes platform-specific trigger detection:
 * - **Android**: Device shake detection
 * - **iOS**: Device shake detection
 * - **JS/WasmJS**: Keyboard shortcut (Shift+D)
 * - **JVM Desktop**: No default trigger (use explicit UI or [DebugMenuWindow])
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun App() {
 *     val debugMenuDialogState = rememberDebugMenuDialogState()
 *
 *     // Your app content
 *     AppContent()
 *
 *     // Debug menu with auto-trigger support
 *     AppDebugMenu.Dialog(state = debugMenuDialogState)
 * }
 * ```
 *
 * @param state The state object for controlling dialog visibility
 * @param enableTrigger Whether to enable the platform-specific trigger (default: true)
 * @param properties Dialog properties for customization
 */
@Composable
fun DebugMenu.Dialog(
    state: DebugMenuDialogState = rememberDebugMenuDialogState(),
    enableTrigger: Boolean = true,
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false,
    ),
) {
    if (enableTrigger) {
        DebugMenuTrigger(onTrigger = state::show)
    }

    if (state.isVisible) {
        Dialog(
            onDismissRequest = state::onDismissRequest,
            properties = properties,
        ) {
            Box(Modifier.padding(top = 40.dp, bottom = 20.dp).padding(horizontal = 20.dp)) {
                View()
            }
        }
    }
}

@Stable
interface DebugMenuDialogState {
    val isVisible: Boolean

    fun show()
    fun onDismissRequest()
}

@Composable
fun rememberDebugMenuDialogState(): DebugMenuDialogState = remember { DebugMenuDialogStateImpl() }

internal class DebugMenuDialogStateImpl(initialIsVisible: Boolean = false) : DebugMenuDialogState {
    override var isVisible by mutableStateOf(initialIsVisible)

    override fun show() {
        isVisible = true
    }

    override fun onDismissRequest() {
        isVisible = false
    }
}
