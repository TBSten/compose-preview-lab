package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window
import org.w3c.dom.url.URL

@Composable
internal actual fun rememberUrlParams(): UrlParams = remember {
    WebUrlParams()
}

private class WebUrlParams : UrlParams {
    private val url = URL(window.location.href)

    override fun get(key: String): String? = url.searchParams.get(key)

    override fun getAll(): Map<String, String> {
        // URLSearchParams.entries() は Kotlin/JS/WasmJS で直接使えないため、
        // 必要なパラメータは個別に get() で取得する
        // この実装は、事前にキーがわかっている場合に使用する
        return emptyMap()
    }
}
