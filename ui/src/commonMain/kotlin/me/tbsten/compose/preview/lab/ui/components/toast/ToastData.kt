package me.tbsten.compose.preview.lab.ui.components.toast

import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

@UiComposePreviewLabApi
enum class ToastType {
    Default,
    Success,
    Error,
    Info,
}

@UiComposePreviewLabApi
enum class ToastDuration(val millis: kotlin.Long) {
    Short(4_000L),
    Long(10_000L),
    Indefinite(kotlin.Long.MAX_VALUE),
}

@UiComposePreviewLabApi
data class ToastAction(val label: String, val onClick: () -> Unit,)

@UiComposePreviewLabApi
data class ToastData(
    val id: Long,
    val message: String,
    val type: ToastType = ToastType.Default,
    val action: ToastAction? = null,
    val duration: ToastDuration = ToastDuration.Short,
    val showCloseButton: Boolean = true,
)
