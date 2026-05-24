package me.tbsten.compose.preview.lab.dokka

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
        val previewId = (code.children.singleOrNull() as? ContentText)?.text.orEmpty()
        val baseUrl = System.getProperty(PreviewLabBaseUrlProperty, PreviewLabDefaultBaseUrl)
        val src = "$baseUrl/?iframe&previewId=$previewId"

        div(classes = "preview-lab-embedded-container") {
            iframe(classes = "preview-lab-embedded") {
                style = "display: block; width: 100%; height: 480px; border: 0; margin: 16px auto;"
                this.src = src
                attributes["loading"] = "lazy"
                attributes["allow"] = "clipboard-read; clipboard-write"
            }
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
