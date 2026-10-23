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
 * This is the recommended way to display a debug menu. The dialog automatically
 * includes platform-specific trigger detection:
 * - **Android**, **iOS**: Device shake
 * - **JS/WasmJS**: Keyboard shortcut (Shift+D)
 * - **JVM Desktop**: No default trigger (use explicit UI or [DebugMenuWindow])
 *
 * # Basic Usage
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     // Application UI
 *     MainContent()
 *
 *     // Debug menu dialog (shown via shake or Shift+D)
 *     AppDebugMenu.Dialog()
 * }
 * ```
 *
 * # Manual Control
 *
 * You can disable the automatic trigger and control visibility manually:
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     val dialogState = rememberDebugMenuDialogState()
 *
 *     Button(onClick = { dialogState.show() }) {
 *         Text("Open Debug Menu")
 *     }
 *
 *     // Disable trigger and control manually
 *     AppDebugMenu.Dialog(
 *         state = dialogState,
 *         trigger = DebugMenuTrigger.None,
 *     )
 * }
 * ```
 *
 * @param state The state object for controlling dialog visibility
 * @param trigger The trigger to use for showing the dialog (default: platform default)
 * @param properties Dialog properties for customization
 *
 * @see DebugMenuTrigger
 * @see rememberDebugMenuDialogState
 */
@Composable
fun DebugMenu.Dialog(
    state: DebugMenuDialogState = rememberDebugMenuDialogState(),
    trigger: DebugMenuTrigger = DebugMenuTrigger.default(),
    properties: DialogProperties = DialogProperties(
        usePlatformDefaultWidth = false,
    ),
) {
    trigger.Effect(onTrigger = state::show)

    if (state.isVisible) {
        Dialog(
            onDismissRequest = state::onDismissRequest,
            properties = properties,
        ) {
            Box(Modifier.padding(top = 40.dp, bottom = 20.dp).padding(horizontal = 20.dp)) {
                TabsView()
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
