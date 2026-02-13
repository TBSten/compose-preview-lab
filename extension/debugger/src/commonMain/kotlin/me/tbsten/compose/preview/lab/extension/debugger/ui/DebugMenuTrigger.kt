package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.runtime.Composable

/**
 * Sealed interface representing platform-specific triggers for showing the debug menu.
 *
 * Each platform provides its own implementations:
 * - **Android**: [Shake detection][DebugMenuTrigger] using accelerometer
 * - **iOS**: [Shake detection][DebugMenuTrigger] using CoreMotion
 * - **JS/WasmJS**: [Keyboard shortcut][DebugMenuTrigger] (default: Shift+D)
 * - **JVM Desktop**: No default trigger available
 *
 * Use [DebugMenuTrigger.None] to disable triggers, or [DebugMenuTrigger.default] to get
 * the platform's default trigger.
 *
 * Example usage:
 * ```kotlin
 * // Use default platform trigger
 * AppDebugMenu.Dialog(trigger = DebugMenuTrigger.default())
 *
 * // Disable trigger
 * AppDebugMenu.Dialog(trigger = DebugMenuTrigger.None)
 *
 * // Platform-specific (JS/WasmJS)
 * AppDebugMenu.Dialog(trigger = DebugMenuTrigger.KeyboardShortcut(key = "M", shift = true))
 * ```
 */
expect sealed interface DebugMenuTrigger {
    /**
     * Composable effect that listens for the trigger and invokes [onTrigger] when detected.
     */
    @Composable
    fun Effect(onTrigger: () -> Unit)

    companion object {
        /**
         * Returns the default trigger for the current platform.
         *
         * - **Android**: Shake detection
         * - **iOS**: Shake detection
         * - **JS/WasmJS**: Keyboard shortcut (Shift+D)
         * - **JVM Desktop**: [None] (no trigger)
         */
        fun default(): DebugMenuTrigger

        /**
         * A trigger that does nothing. Use this to disable automatic trigger detection.
         */
        val None: DebugMenuTrigger
    }
}

/**
 * Legacy composable function for backward compatibility.
 *
 * @param onTrigger Callback invoked when the trigger action is detected
 * @see DebugMenuTrigger
 */
@Composable
fun DebugMenuTrigger(onTrigger: () -> Unit) {
    DebugMenuTrigger.default().Effect(onTrigger)
}
