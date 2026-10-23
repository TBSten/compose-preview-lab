package me.tbsten.compose.preview.lab.dokka

import java.net.URLEncoder
import java.util.Locale
import kotlinx.html.FlowContent
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.iframe
import kotlinx.html.pre
import kotlinx.html.span
import kotlinx.html.style
import org.jetbrains.dokka.base.renderers.html.HtmlRenderer
import org.jetbrains.dokka.pages.ContentCodeBlock
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.plugability.DokkaContext

internal const val PreviewLabBaseUrlProperty: String = "previewLab.dokka.baseUrl"
internal const val PreviewLabDefaultBaseUrl: String = "compose-preview-lab-gallery"

// `<width>x<height>` (integers, lowercase `x`). Used as the iframe's CSS
// `aspect-ratio` so the embedded preview keeps its shape across screen widths.
internal val PreviewLabSizeRegex: Regex = Regex("""^(\d+)x(\d+)$""")

internal data class PreviewLabTagPayload(val previewId: String, val aspectRatio: Pair<Int, Int>?) {
    companion object {
        fun parse(raw: String): PreviewLabTagPayload? {
            val tokens = raw.trim().split(Regex("""\s+"""))
            val previewId = tokens.firstOrNull()?.takeIf { it.isNotEmpty() } ?: return null
            val aspectRatio = tokens.drop(1)
                .firstNotNullOfOrNull { token -> parseAspectRatio(token) }
            return PreviewLabTagPayload(previewId, aspectRatio)
        }

        // Returns null for anything that isn't a strictly positive `<w>x<h>` token.
        // Uses `toIntOrNull` so values that exceed `Int.MAX_VALUE` (e.g. 999999999999x1)
        // safely fall back to the default sizing instead of throwing `NumberFormatException`
        // and breaking Dokka generation.
        private fun parseAspectRatio(token: String): Pair<Int, Int>? {
            val match = PreviewLabSizeRegex.matchEntire(token) ?: return null
            val (wRaw, hRaw) = match.destructured
            val w = wRaw.toIntOrNull()?.takeIf { it > 0 } ?: return null
            val h = hRaw.toIntOrNull()?.takeIf { it > 0 } ?: return null
            return w to h
        }
    }
}

// Known limitation: Dokka v2 runs the generator in a forked worker
// (ProcessIsolation), so `-DpreviewLab.dokka.baseUrl=...` set on the Gradle
// invocation or via `systemProp.` in `gradle.properties` does NOT reach this
// renderer. A reliable override path is the next milestone — exposing a
// typed `DokkaPluginParametersBaseSpec` (ConfigurableBlock) consumers set
// in the `dokka { pluginsConfiguration { ... } }` DSL. Until then the
// property is honoured only in in-process callers (e.g. the unit tests),
// and real-build callers get the default base URL.

public open class PreviewLabHtmlRenderer(context: DokkaContext) : HtmlRenderer(context) {
    override fun FlowContent.buildCodeBlock(code: ContentCodeBlock, pageContext: ContentPage) {
        if (code.language == PreviewLabIframeCode) {
            renderPreviewLabIframe(code)
        } else {
            renderDefaultSampleContainer(code, pageContext)
        }
    }

    private fun FlowContent.renderPreviewLabIframe(code: ContentCodeBlock) {
        val rawPayload = (code.children.singleOrNull() as? ContentText)?.text.orEmpty()
        val payload = PreviewLabTagPayload.parse(rawPayload) ?: return
        val baseUrl = resolveBaseUrl()
        val encodedPreviewId = URLEncoder.encode(payload.previewId, Charsets.UTF_8)
        val src = "$baseUrl/?iframe&previewId=$encodedPreviewId"

        // Default sizing keeps a fixed 720px height (enough for the full PreviewLab UI on a
        // typical doc-site column width without making the canvas Compose wasm allocates
        // look blurry on high-DPI screens). When the tag explicitly specifies `WxH`, switch
        // to aspect-ratio so the iframe scales with the column while preserving the ratio.
        // `calc(100% - 32px)` leaves 16px on each side once `margin: 16px` is applied,
        // so the iframe is fully inset within its container without horizontal overflow.
        val sizeStyle = when (val ar = payload.aspectRatio) {
            null -> "width: calc(100% - 32px); height: 720px;"
            else -> "width: calc(100% - 32px); aspect-ratio: ${ar.first} / ${ar.second}; height: auto;"
        }

        div(classes = "preview-lab-embedded-container") {
            iframe(classes = "preview-lab-embedded") {
                style = "display: block; $sizeStyle border: 2px solid #d0d7de; margin: 16px;"
                this.src = src
                attributes["loading"] = "lazy"
                attributes["allow"] = "clipboard-read; clipboard-write"
            }
        }
    }

    // Normalises the configured base URL so it composes cleanly with `"/?iframe&..."`.
    // - blank / unset → default
    // - trims surrounding whitespace
    // - strips trailing `/` so we never emit `//?...`
    private fun resolveBaseUrl(): String {
        val raw = System.getProperty(PreviewLabBaseUrlProperty)?.trim().orEmpty()
        val normalised = raw.ifEmpty { PreviewLabDefaultBaseUrl }
        return normalised.trimEnd('/')
    }

    // Mirrors the default HtmlRenderer fallback for non-iframe code blocks; we cannot call
    // `super.buildCodeBlock(...)` because Kotlin does not allow super-calls on overridden
    // extension functions.
    private fun FlowContent.renderDefaultSampleContainer(code: ContentCodeBlock, pageContext: ContentPage) {
        div("sample-container") {
            val codeLang = "lang-" + code.language.ifEmpty { "kotlin" }
            val stylesWithBlock = code.style + TextStyle.Block + codeLang
            pre {
                code(stylesWithBlock.joinToString(" ") { it.toString().lowercase(Locale.ROOT) }) {
                    attributes["theme"] = "idea"
                    code.children.forEach { buildContentNode(it, pageContext) }
                }
            }
            if (!code.style.contains(ContentStyle.RunnableSample)) {
                span(classes = "top-right-position") {
                    span("copy-icon")
                    div("copy-popup-wrapper popup-to-left") {
                        span("copy-popup-icon")
                        span { text("Content copied to clipboard") }
                    }
                }
            }
        }
    }
}
