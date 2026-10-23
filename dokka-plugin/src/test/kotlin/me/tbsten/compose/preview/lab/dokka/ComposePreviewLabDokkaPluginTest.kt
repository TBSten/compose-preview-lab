package me.tbsten.compose.preview.lab.dokka

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.DokkaConfigurationImpl
import org.jetbrains.dokka.PluginConfigurationImpl
import org.jetbrains.dokka.base.testApi.testRunner.BaseAbstractTest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Test
import utils.TestOutputWriterPlugin

class ComposePreviewLabDokkaPluginTest : BaseAbstractTest() {
    private val configuration = configurationWith()

    /**
     * Builds a test configuration. [pluginJson] is handed to the plugin the same way the Dokka Gradle
     * plugin does it (`pluginsConfiguration`, JSON serialised). [partial] mimics a module rendered as part
     * of a multi-module site (`delayTemplateSubstitution = true`).
     */
    private fun configurationWith(pluginJson: String? = null, partial: Boolean = false): DokkaConfigurationImpl =
        dokkaConfiguration {
            delayTemplateSubstitution = partial
            if (pluginJson != null) {
                pluginsConfigurations = mutableListOf(
                    PluginConfigurationImpl(
                        fqPluginName = ComposePreviewLabDokkaPlugin::class.qualifiedName!!,
                        serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                        values = pluginJson,
                    ),
                )
            }
            sourceSets {
                sourceSet {
                    sourceRoots = listOf("src/main/kotlin")
                }
            }
        }

    /** Renders [kdoc] on `fun <functionName>()` and returns the parsed page of that function. */
    private fun renderPage(
        functionName: String,
        kdoc: String,
        configuration: DokkaConfigurationImpl = this.configuration,
        pageFileName: String = functionName.toDokkaFileName(),
    ): Document {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * ${kdoc.lines().joinToString("\n| * ")}
            | */
            |fun $functionName(): String = "x"
        """.trimMargin()

        lateinit var document: Document
        testInline(
            source,
            configuration,
            pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
        ) {
            renderingStage = { _, _ ->
                val contents = writerPlugin.writer.contents
                // Partial (multi-module) output has no module directory: the aggregating build adds it.
                val key = if (configuration.delayTemplateSubstitution) {
                    "example/$pageFileName.html"
                } else {
                    "root/example/$pageFileName.html"
                }
                val html = contents[key] ?: error("page $key not rendered; available: ${contents.keys.sorted()}")
                document = Jsoup.parse(html)
            }
        }
        return document
    }

    // `withPreviewLab` → `with-preview-lab`, matching Dokka's default file naming.
    private fun String.toDokkaFileName(): String = replace(Regex("([A-Z])")) { "-" + it.value.lowercase() }

    private fun Document.iframe() = select("iframe.preview-lab-embedded").first()!!

    private fun Document.container() = select("div.preview-lab-embedded-container").first()!!

    @Test
    fun `previewLab tag is rendered as iframe with previewId in src`() {
        val doc = renderPage("withPreviewLab", "Function with previewLab tag.\n@previewLab MyPreview_FooId")

        doc.select("iframe.preview-lab-embedded") shouldHaveSize 1
        doc.select("div.preview-lab-embedded-container") shouldHaveSize 1

        val iframe = doc.iframe()
        iframe.attr("src") shouldContain "?iframe&previewId=MyPreview_FooId"
        iframe.attr("src") shouldEndWith "previewId=MyPreview_FooId"
        iframe.attr("loading") shouldBe "lazy"
        iframe.attr("allow") shouldBe "clipboard-read; clipboard-write"
    }

    @Test
    fun `function without previewLab tag does not produce an iframe`() {
        val doc = renderPage("plain", "Plain function without any tags.")

        doc.select("iframe").shouldHaveSize(0)
        doc.select("div.preview-lab-embedded-container").shouldHaveSize(0)
    }

    // ---- base URL ----------------------------------------------------------------------------

    @Test
    fun `default base URL is relative to the site root, not to the page directory`() {
        val src = renderPage("defaultBase", "@previewLab Foo").iframe().attr("src")

        // The page lives at `root/example/default-base.html`, two levels below the site root.
        src shouldBe "../../${ComposePreviewLabDokkaConfiguration.DefaultBaseUrl}/?iframe&previewId=Foo"
    }

    @Test
    fun `absolute base URL from plugin configuration is used verbatim`() {
        val src = renderPage(
            "withOverride",
            "@previewLab AnotherPreview",
            configurationWith("""{"baseUrl": "https://example.com/lab"}"""),
        ).iframe().attr("src")

        src shouldBe "https://example.com/lab/?iframe&previewId=AnotherPreview"
    }

    @Test
    fun `root-relative base URL is not prefixed with the path to root`() {
        val src = renderPage(
            "rootRelative",
            "@previewLab Foo",
            configurationWith("""{"baseUrl": "/docs/gallery/"}"""),
        ).iframe().attr("src")

        src shouldBe "/docs/gallery/?iframe&previewId=Foo"
    }

    @Test
    fun `trailing slash and surrounding whitespace in baseUrl are normalised`() {
        val src = renderPage(
            "normalised",
            "@previewLab Foo",
            configurationWith("""{"baseUrl": "  https://example.com/lab/  "}"""),
        ).iframe().attr("src")

        src shouldBe "https://example.com/lab/?iframe&previewId=Foo"
    }

    @Test
    fun `blank baseUrl falls back to the default value`() {
        val src = renderPage(
            "blank",
            "@previewLab Foo",
            configurationWith("""{"baseUrl": "   "}"""),
        ).iframe().attr("src")

        src shouldBe "../../${ComposePreviewLabDokkaConfiguration.DefaultBaseUrl}/?iframe&previewId=Foo"
    }

    @Test
    fun `relative base URL in a partial (multi-module) build is wrapped in a path-to-root template command`() {
        val doc = renderPage("partialModule", "@previewLab Foo", configurationWith(partial = true))

        // The aggregating build resolves `###` to the real path to root once the module is placed in the site.
        // (Dokka emits other template commands on the page too, so look for the one wrapping our iframe.)
        val command = doc.select("dokka-template-command:has(iframe.preview-lab-embedded)").first()!!
        command.attr("data") shouldContain "PathToRootSubstitutionCommand"
        command.attr("data") shouldContain "\"pattern\":\"###\""
        doc.iframe().attr("src") shouldBe "###${ComposePreviewLabDokkaConfiguration.DefaultBaseUrl}/?iframe&previewId=Foo"
    }

    @Test
    fun `absolute base URL in a partial build needs no template command`() {
        val doc = renderPage(
            "partialAbsolute",
            "@previewLab Foo",
            configurationWith("""{"baseUrl": "https://example.com/lab"}""", partial = true),
        )

        doc.select("dokka-template-command:has(iframe.preview-lab-embedded)") shouldHaveSize 0
        doc.iframe().attr("src") shouldBe "https://example.com/lab/?iframe&previewId=Foo"
    }

    @Test
    fun `previewId is URL-encoded when constructing the iframe src`() {
        // `=` survives Dokka's KDoc parser (it is not a tag separator) but has URL-reserved meaning in the
        // query string. URLEncoder turns it into `%3D`; otherwise it would leak verbatim as `previewId=a=b`
        // and break query-string parsing on the gallery side.
        val src = renderPage("encoded", "@previewLab a=b").iframe().attr("src")

        src shouldEndWith "previewId=a%3Db"
        src shouldNotContain "previewId=a=b"
    }

    // ---- sizing ------------------------------------------------------------------------------

    @Test
    fun `previewLab tag without size keeps the default fixed-height style`() {
        val style = renderPage("defaultSized", "@previewLab DefaultSized").iframe().attr("style")

        style shouldContain "height: ${ComposePreviewLabDokkaConfiguration.IframeStyle.DefaultHeight}"
        style shouldNotContain "aspect-ratio"
    }

    @Test
    fun `previewLab tag with WxH suffix emits aspect-ratio instead of fixed height`() {
        val iframe = renderPage("custom", "@previewLab Custom 1280x980").iframe()

        iframe.attr("style") shouldContain "aspect-ratio: 1280 / 980"
        iframe.attr("style") shouldContain "height: auto"
        iframe.attr("style") shouldNotContain "720px"
        // previewId itself must not include the size token.
        iframe.attr("src") shouldEndWith "previewId=Custom"
    }

    @Test
    fun `previewLab tag with malformed size token falls back to default sizing`() {
        val iframe = renderPage("malformed", "@previewLab Foo not-a-size").iframe()

        iframe.attr("style") shouldContain "height: 720px"
        iframe.attr("src") shouldEndWith "previewId=Foo"
    }

    @Test
    fun `previewLab tag rejects non-positive WxH and falls back to default sizing`() {
        val style = renderPage("zeroSized", "@previewLab ZeroSized 0x0").iframe().attr("style")

        style shouldContain "height: 720px"
        style shouldNotContain "aspect-ratio"
    }

    @Test
    fun `previewLab tag falls back to default when WxH overflows Int range`() {
        val style = renderPage("huge", "@previewLab Huge 999999999999x1").iframe().attr("style")

        style shouldContain "height: 720px"
        style shouldNotContain "aspect-ratio"
    }

    // ---- iframe style ------------------------------------------------------------------------

    @Test
    fun `default iframe style is a bordered, inset, full-width frame`() {
        val doc = renderPage("defaultStyle", "@previewLab Foo")

        doc.container().attr("style") shouldBe "padding: 16px;"
        val style = doc.iframe().attr("style")
        style shouldStartWith "display: block; width: 100%; box-sizing: border-box;"
        style shouldContain "border: 2px solid #d0d7de;"
        style shouldContain "border-radius: 0;"
        style shouldContain "background-color: transparent;"
    }

    @Test
    fun `iframe style can be customised through plugin configuration`() {
        val doc = renderPage(
            "styled",
            "@previewLab Foo",
            configurationWith(
                """
                {
                  "iframe": {
                    "defaultHeight": "480px",
                    "border": "1px dashed red",
                    "borderRadius": "12px",
                    "margin": "4px 8px",
                    "backgroundColor": "#fff"
                  }
                }
                """.trimIndent(),
            ),
        )

        doc.container().attr("style") shouldBe "padding: 4px 8px;"
        val style = doc.iframe().attr("style")
        style shouldContain "height: 480px;"
        style shouldContain "border: 1px dashed red;"
        style shouldContain "border-radius: 12px;"
        style shouldContain "background-color: #fff;"
    }

    @Test
    fun `partially specified iframe style keeps defaults for the rest`() {
        val doc = renderPage(
            "partialStyle",
            "@previewLab Foo",
            configurationWith("""{"baseUrl": "https://example.com/lab", "iframe": {"border": "none"}}"""),
        )

        doc.container().attr("style") shouldBe "padding: 16px;"
        val style = doc.iframe().attr("style")
        style shouldContain "border: none;"
        style shouldContain "height: 720px;"
        doc.iframe().attr("src") shouldBe "https://example.com/lab/?iframe&previewId=Foo"
    }

    // ---- regression --------------------------------------------------------------------------

    @Test
    fun `regular kotlin code blocks are still rendered as sample-container`() {
        val doc = renderPage(
            "withCodeSample",
            "Function whose KDoc includes a plain kotlin code sample:\n```\nval x = 1\n```",
        )

        doc.select("iframe.preview-lab-embedded").shouldHaveSize(0)
        doc.select("div.sample-container") shouldHaveSize 1
    }
}
