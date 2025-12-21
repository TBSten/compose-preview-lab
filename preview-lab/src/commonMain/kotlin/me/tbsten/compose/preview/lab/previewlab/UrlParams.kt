package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/**
 * URLパラメータを取得するためのインターフェース。
 * Webプラットフォームではブラウザの URL から、それ以外では空のMapを返す。
 */
internal interface UrlParams {
    /**
     * 指定されたキーのURLパラメータ値を取得する。
     * @param key パラメータ名
     * @return パラメータ値、存在しない場合はnull
     */
    operator fun get(key: String): String?

    /**
     * すべてのURLパラメータを取得する。
     */
    fun getAll(): Map<String, String>

    companion object {
        val Empty: UrlParams = object : UrlParams {
            override fun get(key: String): String? = null
            override fun getAll(): Map<String, String> = emptyMap()
        }
    }
}

/**
 * 現在のURLパラメータを提供するCompositionLocal。
 */
internal val LocalUrlParams = compositionLocalOf<UrlParams> { UrlParams.Empty }

/**
 * 現在のURLパラメータを取得する。
 * Webプラットフォームではブラウザの URL.searchParams から取得し、
 * それ以外のプラットフォームでは空のMapを返す。
 */
@Composable
internal expect fun rememberUrlParams(): UrlParams
