package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberUrlParams(): UrlParams = EmptyUrlParams

private object EmptyUrlParams : UrlParams {
    override fun get(key: String): String? = null
    override fun getAll(): Map<String, String> = emptyMap()
}
