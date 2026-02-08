package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.runtime.Composable

/**
 * JVM Desktop implementation of DebugMenuTrigger.
 *
 * On JVM Desktop, there's no default trigger behavior since window management
 * is typically handled differently. Use [DebugMenu.Window] or explicit UI buttons
 * to show the debug menu on desktop.
 *
 * @see me.tbsten.compose.preview.lab.extension.debugger.DebugMenu.Window
 */
@Composable
actual fun DebugMenuTrigger(onTrigger: () -> Unit,) {
    // No-op on JVM Desktop
    // Use DebugMenu.Window or explicit UI buttons instead
}
