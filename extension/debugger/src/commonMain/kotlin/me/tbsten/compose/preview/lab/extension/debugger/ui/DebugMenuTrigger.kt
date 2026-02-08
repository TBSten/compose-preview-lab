package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.runtime.Composable

/**
 * A composable that detects platform-specific trigger actions to show the debug menu.
 *
 * The trigger behavior varies by platform:
 * - **Android**: Device shake detection using accelerometer
 * - **iOS**: Device shake detection using CoreMotion
 * - **JS/WasmJS**: Keyboard shortcut (Shift+D)
 * - **JVM Desktop**: No default trigger (use explicit UI button)
 *
 * Example usage:
 * ```kotlin
 * val debugMenuDialogState = rememberDebugMenuDialogState()
 *
 * DebugMenuTrigger(onTrigger = { debugMenuDialogState.show() })
 *
 * if (debugMenuDialogState.isVisible) {
 *     AppDebugMenu.Dialog(state = debugMenuDialogState)
 * }
 * ```
 *
 * @param onTrigger Callback invoked when the trigger action is detected
 */
@Composable
expect fun DebugMenuTrigger(onTrigger: () -> Unit)
