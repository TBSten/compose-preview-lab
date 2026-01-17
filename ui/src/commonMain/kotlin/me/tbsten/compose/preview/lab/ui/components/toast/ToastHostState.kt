package me.tbsten.compose.preview.lab.ui.components.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

@Stable
@InternalComposePreviewLabApi
class ToastHostState {
    private val _toasts = mutableStateListOf<ToastData>()
    val toasts: List<ToastData> get() = _toasts

    // Note: This counter is only accessed from the main thread (Compose UI thread),
    // so thread-safety is not required for this use case.
    private var nextId = 0L

    fun show(
        message: String,
        type: ToastType = ToastType.Default,
        action: ToastAction? = null,
        duration: ToastDuration = ToastDuration.Short,
        showCloseButton: Boolean = true,
    ): Long {
        val id = nextId++
        _toasts.add(
            ToastData(
                id = id,
                message = message,
                type = type,
                action = action,
                duration = duration,
                showCloseButton = showCloseButton,
            ),
        )
        return id
    }

    fun dismiss(id: Long) {
        _toasts.removeAll { it.id == id }
    }

    internal fun removeToast(toast: ToastData) {
        _toasts.remove(toast)
    }
}

@Composable
@InternalComposePreviewLabApi
fun rememberToastHostState(): ToastHostState = remember { ToastHostState() }
