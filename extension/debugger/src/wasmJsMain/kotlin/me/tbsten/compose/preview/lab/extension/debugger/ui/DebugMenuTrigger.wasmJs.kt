package me.tbsten.compose.preview.lab.extension.debugger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.document
import org.w3c.dom.events.KeyboardEvent

/**
 * WasmJS implementation of DebugMenuTrigger using keyboard shortcut (Shift+D).
 */
@Composable
actual fun DebugMenuTrigger(onTrigger: () -> Unit,) {
    DisposableEffect(onTrigger) {
        val listener: (org.w3c.dom.events.Event) -> Unit = { event ->
            val keyEvent = event as? KeyboardEvent
            if (keyEvent != null && keyEvent.shiftKey && keyEvent.key == "D") {
                onTrigger()
            }
        }

        document.addEventListener("keydown", listener)

        onDispose {
            document.removeEventListener("keydown", listener)
        }
    }
}
