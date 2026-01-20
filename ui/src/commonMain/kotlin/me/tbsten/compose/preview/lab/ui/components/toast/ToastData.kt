package me.tbsten.compose.preview.lab.ui.components.toast

import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

@InternalComposePreviewLabApi
public enum class ToastType {
    Default,
    Success,
    Error,
    Info,
}

@InternalComposePreviewLabApi
public enum class ToastDuration(public val millis: kotlin.Long) {
    Short(4_000L),
    Long(10_000L),
    Indefinite(kotlin.Long.MAX_VALUE),
}

@InternalComposePreviewLabApi
public data class ToastAction(public val label: String, public val onClick: () -> Unit)

@InternalComposePreviewLabApi
public data class ToastData(
    public val id: Long,
    public val message: String,
    public val type: ToastType = ToastType.Default,
    public val action: ToastAction? = null,
    public val duration: ToastDuration = ToastDuration.Short,
    public val showCloseButton: Boolean = true,
)
