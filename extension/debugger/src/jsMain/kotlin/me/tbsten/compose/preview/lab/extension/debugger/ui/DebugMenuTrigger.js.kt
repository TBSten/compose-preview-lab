package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.document
import org.w3c.dom.events.KeyboardEvent

/**
 * JS implementation of DebugMenuTrigger.
 */
actual sealed interface DebugMenuTrigger {
    @Composable
    actual fun Effect(onTrigger: () -> Unit)

    actual companion object {
        actual fun default(): DebugMenuTrigger = KeyboardShortcut()
        actual val None: DebugMenuTrigger = NoneImpl
    }

    private data object NoneImpl : DebugMenuTrigger {
        @Composable
        override fun Effect(onTrigger: () -> Unit) {
            // No-op
        }
    }

    /**
     * Trigger that detects keyboard shortcuts.
     *
     * @param key The key to detect (default: "D")
     * @param shift Whether Shift must be held (default: true)
     * @param ctrl Whether Ctrl must be held (default: false)
     * @param alt Whether Alt must be held (default: false)
     * @param meta Whether Meta (Cmd on Mac) must be held (default: false)
     */
    data class KeyboardShortcut(
        val key: String = "D",
        val shift: Boolean = true,
        val ctrl: Boolean = false,
        val alt: Boolean = false,
        val meta: Boolean = false,
    ) : DebugMenuTrigger {
        @Composable
        override fun Effect(onTrigger: () -> Unit) {
            DisposableEffect(onTrigger, key, shift, ctrl, alt, meta) {
                val listener: (org.w3c.dom.events.Event) -> Unit = { event ->
                    val keyEvent = event as? KeyboardEvent
                    if (keyEvent != null &&
                        keyEvent.key == key &&
                        keyEvent.shiftKey == shift &&
                        keyEvent.ctrlKey == ctrl &&
                        keyEvent.altKey == alt &&
                        keyEvent.metaKey == meta
                    ) {
                        onTrigger()
                    }
                }

                document.addEventListener("keydown", listener)

                onDispose {
                    document.removeEventListener("keydown", listener)
                }
            }
        }
    }
}
