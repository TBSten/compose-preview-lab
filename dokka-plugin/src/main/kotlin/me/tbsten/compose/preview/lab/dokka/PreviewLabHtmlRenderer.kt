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
import org.jetbrains.dokka.base.renderers.html.templateCommand
import org.jetbrains.dokka.base.templating.PathToRootSubstitutionCommand
import org.jetbrains.dokka.pages.ContentCodeBlock
import org.jetbrains.dokka.pages.ContentPage
import org.jetbrains.dokka.pages.ContentStyle
import org.jetbrains.dokka.pages.ContentText
import org.jetbrains.dokka.pages.TextStyle
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.configuration

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

/**
 * Normalises [ComposePreviewLabDokkaConfiguration.baseUrl] so it composes cleanly with `"?iframe&..."`.
 *
 * - blank → [ComposePreviewLabDokkaConfiguration.DefaultBaseUrl]
 * - surrounding whitespace and a leading `./` are dropped
 * - trailing `/` is stripped so we never emit `//?...`
 */
internal data class PreviewLabBaseUrl(val value: String, val isAbsolute: Boolean) {
    /** `<value>/<query>` – or just `/<query>` for a bare `/` (site root). */
    fun join(query: String): String = if (value.isEmpty()) "/$query" else "$value/$query"

    companion object {
        private val schemeRegex = Regex("""^[A-Za-z][A-Za-z0-9+.\-]*:""")

        fun parse(raw: String): PreviewLabBaseUrl {
            val trimmed = raw.trim().ifEmpty { ComposePreviewLabDokkaConfiguration.DefaultBaseUrl }
            val isAbsolute = trimmed.startsWith("/") || schemeRegex.containsMatchIn(trimmed)
            return PreviewLabBaseUrl(
                value = trimmed.removePrefix("./").trimEnd('/'),
                isAbsolute = isAbsolute,
            )
        }
    }
}

/** Placeholder that Dokka replaces with the page's path to the site root (`../../`, …). */
private const val PathToRootPattern: String = "###"

/**
 * [HtmlRenderer] that turns the virtual `@previewLab/iframe` code block produced by
 * [PreviewLabTagContentProvider] into an `<iframe>` pointing at the hosted Compose Preview Lab gallery.
 *
 * ```kotlin
 * /** @previewLab MyButtonPreview 1280x680 */
 * fun MyButton() {}
 * ```
 *
 * renders (with the default configuration, on a page two directories below the site root) as
 *
 * ```html
 * <div class="preview-lab-embedded-container" style="padding: 16px;">
 *   <iframe class="preview-lab-embedded"
 *           src="../../compose-preview-lab-gallery/?iframe&previewId=MyButtonPreview"
 *           loading="lazy" allow="clipboard-read; clipboard-write"
 *           style="display: block; width: 100%; box-sizing: border-box; aspect-ratio: 1280 / 680; height: auto; border: 2px solid #d0d7de; border-radius: 0; background-color: transparent;">
 *   </iframe>
 * </div>
 * ```
 *
 * Base URL and iframe style come from [ComposePreviewLabDokkaConfiguration] (Dokka `pluginsConfiguration`).
 */
public open class PreviewLabHtmlRenderer(context: DokkaContext) : HtmlRenderer(context) {
    private val pluginConfiguration: ComposePreviewLabDokkaConfiguration =
        configuration<ComposePreviewLabDokkaPlugin, ComposePreviewLabDokkaConfiguration>(context)
            ?: ComposePreviewLabDokkaConfiguration()

    override fun FlowContent.buildCodeBlock(code: ContentCodeBlock, pageContext: ContentPage) {
        if (code.language == PreviewLabIframeCode) {
            renderPreviewLabIframe(code, pageContext)
        } else {
            renderDefaultSampleContainer(code, pageContext)
        }
    }

    private fun FlowContent.renderPreviewLabIframe(code: ContentCodeBlock, pageContext: ContentPage) {
        val rawPayload = (code.children.singleOrNull() as? ContentText)?.text.orEmpty()
        val payload = PreviewLabTagPayload.parse(rawPayload) ?: return
        val baseUrl = PreviewLabBaseUrl.parse(pluginConfiguration.baseUrl)
        val query = "?iframe&previewId=" + URLEncoder.encode(payload.previewId, Charsets.UTF_8)
        val iframeStyle = pluginConfiguration.iframe

        div(classes = "preview-lab-embedded-container") {
            style = "padding: ${iframeStyle.margin};"
            if (baseUrl.isAbsolute) {
                previewLabIframe(src = baseUrl.join(query), payload = payload, iframeStyle = iframeStyle)
            } else {
                // A relative base URL is resolved against the root of the generated site rather than the
                // page's own directory, so a single `compose-preview-lab-gallery/` next to `index.html`
                // serves every page. `###` is substituted with the page's path-to-root right away in
                // single-module builds, and by the aggregating build when this module is rendered as a
                // partial of a multi-module site.
                templateCommand(
                    PathToRootSubstitutionCommand(
                        pattern = PathToRootPattern,
                        default = locationProvider.pathToRoot(pageContext),
                    ),
                ) {
                    previewLabIframe(
                        src = PathToRootPattern + baseUrl.join(query),
                        payload = payload,
                        iframeStyle = iframeStyle,
                    )
                }
            }
        }
    }

    private fun FlowContent.previewLabIframe(
        src: String,
        payload: PreviewLabTagPayload,
        iframeStyle: ComposePreviewLabDokkaConfiguration.IframeStyle,
    ) {
        // Without `WxH` the iframe gets a fixed height (enough for the full PreviewLab UI on a typical
        // doc-site column without making the canvas Compose wasm allocates look blurry on high-DPI
        // screens). With `WxH` it scales with the column while preserving the requested ratio.
        val sizeStyle = when (val ar = payload.aspectRatio) {
            null -> "height: ${iframeStyle.defaultHeight};"
            else -> "aspect-ratio: ${ar.first} / ${ar.second}; height: auto;"
        }
        iframe(classes = "preview-lab-embedded") {
            style = "display: block; width: 100%; box-sizing: border-box; $sizeStyle " +
                "border: ${iframeStyle.border}; border-radius: ${iframeStyle.borderRadius}; " +
                "background-color: ${iframeStyle.backgroundColor};"
            this.src = src
            attributes["loading"] = "lazy"
            attributes["allow"] = "clipboard-read; clipboard-write"
        }
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
