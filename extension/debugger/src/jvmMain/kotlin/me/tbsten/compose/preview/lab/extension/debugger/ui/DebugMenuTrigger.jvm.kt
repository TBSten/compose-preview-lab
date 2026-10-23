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
actual sealed interface DebugMenuTrigger {
    @Composable
    actual fun Effect(onTrigger: () -> Unit)

    actual companion object {
        /**
         * Returns [None] on JVM Desktop since there's no default trigger.
         * Use [DebugMenu.Window] or explicit UI buttons instead.
         */
        actual fun default(): DebugMenuTrigger = None
        actual val None: DebugMenuTrigger = NoneImpl
    }

    private data object NoneImpl : DebugMenuTrigger {
        @Composable
        override fun Effect(onTrigger: () -> Unit) {
            // No-op on JVM Desktop
        }
    }
}
