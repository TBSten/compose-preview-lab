package me.tbsten.compose.preview.lab.dokka

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldNotContain
import org.jetbrains.dokka.base.testApi.testRunner.BaseAbstractTest
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import utils.TestOutputWriterPlugin

class ComposePreviewLabDokkaPluginTest : BaseAbstractTest() {
    private val configuration = dokkaConfiguration {
        sourceSets {
            sourceSet {
                sourceRoots = listOf("src/main/kotlin")
            }
        }
    }

    @Test
    fun `previewLab tag is rendered as iframe with previewId in src`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * Function with previewLab tag.
            | * @previewLab MyPreview_FooId
            | */
            |fun withPreviewLab(): String = "yes"
        """.trimIndent()

        testInline(
            source,
            configuration,
            pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
        ) {
            renderingStage = { _, _ ->
                val html = writerPlugin.writer.contents.getValue("root/example/with-preview-lab.html")
                val doc = Jsoup.parse(html)
                val iframes = doc.select("iframe.preview-lab-embedded")

                iframes.size shouldBe 1
                val src = iframes.first()!!.attr("src")
                src shouldContain "?iframe&previewId=MyPreview_FooId"
                src shouldEndWith "previewId=MyPreview_FooId"

                val container = doc.select("div.preview-lab-embedded-container")
                container.size shouldBe 1
            }
        }
    }

    @Test
    fun `function without previewLab tag does not produce an iframe`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * Plain function without any tags.
            | */
            |fun plain(): String = "plain"
        """.trimIndent()

        testInline(
            source,
            configuration,
            pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
        ) {
            renderingStage = { _, _ ->
                val html = writerPlugin.writer.contents.getValue("root/example/plain.html")
                val doc = Jsoup.parse(html)
                doc.select("iframe").shouldHaveSize(0)
                doc.select("div.preview-lab-embedded-container").shouldHaveSize(0)
            }
        }
    }

    // NOTE: `BaseAbstractTest` runs Dokka in-process so System property
    // is visible to the renderer. In real Gradle builds the Dokka worker is
    // a forked JVM (ProcessIsolation) and the property does NOT reach the
    // renderer. See the KNOWN LIMITATION comment in PreviewLabHtmlRenderer.kt.
    @Test
    fun `previewLab base URL can be overridden via system property (in-process only)`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * @previewLab AnotherPreview
            | */
            |fun withOverride(): String = "x"
        """.trimIndent()

        val previousValue = System.getProperty(PreviewLabBaseUrlProperty)
        System.setProperty(PreviewLabBaseUrlProperty, "https://example.com/lab")
        try {
            testInline(
                source,
                configuration,
                pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
            ) {
                renderingStage = { _, _ ->
                    val html = writerPlugin.writer.contents.getValue("root/example/with-override.html")
                    val src = Jsoup.parse(html).select("iframe.preview-lab-embedded").first()!!.attr("src")
                    src shouldBe "https://example.com/lab/?iframe&previewId=AnotherPreview"
                }
            }
        } finally {
            if (previousValue == null) {
                System.clearProperty(PreviewLabBaseUrlProperty)
            } else {
                System.setProperty(PreviewLabBaseUrlProperty, previousValue)
            }
        }
    }

    @Test
    fun `previewLab tag without size keeps the default fixed-height style`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * @previewLab DefaultSized
            | */
            |fun defaultSized(): String = "x"
        """.trimIndent()

        testInline(
            source,
            configuration,
            pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
        ) {
            renderingStage = { _, _ ->
                val html = writerPlugin.writer.contents.getValue("root/example/default-sized.html")
                val style = Jsoup.parse(html).select("iframe.preview-lab-embedded").first()!!.attr("style")
                style shouldContain "height: 720px"
                style shouldNotContain "aspect-ratio"
            }
        }
    }

    @Test
    fun `previewLab tag with WxH suffix emits aspect-ratio instead of fixed height`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * @previewLab Custom 1280x980
            | */
            |fun custom(): String = "x"
        """.trimIndent()

        testInline(
            source,
            configuration,
            pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
        ) {
            renderingStage = { _, _ ->
                val html = writerPlugin.writer.contents.getValue("root/example/custom.html")
                val iframe = Jsoup.parse(html).select("iframe.preview-lab-embedded").first()!!
                val style = iframe.attr("style")
                style shouldContain "aspect-ratio: 1280 / 980"
                style shouldContain "height: auto"
                style shouldNotContain "720px"

                // previewId itself must not include the size token.
                iframe.attr("src") shouldEndWith "previewId=Custom"
            }
        }
    }

    @Test
    fun `previewLab tag with malformed size token falls back to default sizing`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * @previewLab Foo not-a-size
            | */
            |fun malformed(): String = "x"
        """.trimIndent()

        testInline(
            source,
            configuration,
            pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
        ) {
            renderingStage = { _, _ ->
                val html = writerPlugin.writer.contents.getValue("root/example/malformed.html")
                val iframe = Jsoup.parse(html).select("iframe.preview-lab-embedded").first()!!
                iframe.attr("style") shouldContain "height: 720px"
                iframe.attr("src") shouldEndWith "previewId=Foo"
            }
        }
    }

    @Test
    fun `previewLab tag rejects non-positive WxH and falls back to default sizing`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * @previewLab ZeroSized 0x0
            | */
            |fun zeroSized(): String = "x"
        """.trimIndent()

        testInline(
            source,
            configuration,
            pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
        ) {
            renderingStage = { _, _ ->
                val html = writerPlugin.writer.contents.getValue("root/example/zero-sized.html")
                val iframe = Jsoup.parse(html).select("iframe.preview-lab-embedded").first()!!
                iframe.attr("style") shouldContain "height: 720px"
                iframe.attr("style") shouldNotContain "aspect-ratio"
            }
        }
    }

    @Test
    fun `previewLab tag falls back to default when WxH overflows Int range`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * @previewLab Huge 999999999999x1
            | */
            |fun huge(): String = "x"
        """.trimIndent()

        testInline(
            source,
            configuration,
            pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
        ) {
            renderingStage = { _, _ ->
                val html = writerPlugin.writer.contents.getValue("root/example/huge.html")
                val iframe = Jsoup.parse(html).select("iframe.preview-lab-embedded").first()!!
                iframe.attr("style") shouldContain "height: 720px"
                iframe.attr("style") shouldNotContain "aspect-ratio"
            }
        }
    }

    @Test
    fun `previewId is URL-encoded when constructing the iframe src`() {
        val writerPlugin = TestOutputWriterPlugin()
        // `=` survives Dokka's KDoc parser (it is not a tag separator) but has URL-reserved
        // meaning in the query string. URLEncoder turns it into `%3D`. Pre-fix this would
        // have leaked verbatim as `previewId=a=b`, breaking query-string parsing on the
        // gallery side.
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * @previewLab a=b
            | */
            |fun encoded(): String = "x"
        """.trimIndent()

        testInline(
            source,
            configuration,
            pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
        ) {
            renderingStage = { _, _ ->
                val html = writerPlugin.writer.contents.getValue("root/example/encoded.html")
                val src = Jsoup.parse(html).select("iframe.preview-lab-embedded").first()!!.attr("src")
                src shouldEndWith "previewId=a%3Db"
                src shouldNotContain "previewId=a=b"
            }
        }
    }

    @Test
    fun `trailing slash and surrounding whitespace in baseUrl are normalised`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * @previewLab Foo
            | */
            |fun normalised(): String = "x"
        """.trimIndent()

        val previousValue = System.getProperty(PreviewLabBaseUrlProperty)
        System.setProperty(PreviewLabBaseUrlProperty, "  https://example.com/lab/  ")
        try {
            testInline(
                source,
                configuration,
                pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
            ) {
                renderingStage = { _, _ ->
                    val html = writerPlugin.writer.contents.getValue("root/example/normalised.html")
                    val src = Jsoup.parse(html).select("iframe.preview-lab-embedded").first()!!.attr("src")
                    src shouldBe "https://example.com/lab/?iframe&previewId=Foo"
                }
            }
        } finally {
            if (previousValue == null) {
                System.clearProperty(PreviewLabBaseUrlProperty)
            } else {
                System.setProperty(PreviewLabBaseUrlProperty, previousValue)
            }
        }
    }

    @Test
    fun `blank baseUrl property falls back to the default value`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * @previewLab Foo
            | */
            |fun blank(): String = "x"
        """.trimIndent()

        val previousValue = System.getProperty(PreviewLabBaseUrlProperty)
        System.setProperty(PreviewLabBaseUrlProperty, "   ")
        try {
            testInline(
                source,
                configuration,
                pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
            ) {
                renderingStage = { _, _ ->
                    val html = writerPlugin.writer.contents.getValue("root/example/blank.html")
                    val src = Jsoup.parse(html).select("iframe.preview-lab-embedded").first()!!.attr("src")
                    src shouldBe "$PreviewLabDefaultBaseUrl/?iframe&previewId=Foo"
                }
            }
        } finally {
            if (previousValue == null) {
                System.clearProperty(PreviewLabBaseUrlProperty)
            } else {
                System.setProperty(PreviewLabBaseUrlProperty, previousValue)
            }
        }
    }

    @Test
    fun `regular kotlin code blocks are still rendered as sample-container`() {
        val writerPlugin = TestOutputWriterPlugin()
        val source = """
            |/src/main/kotlin/test/Test.kt
            |package example
            |
            |/**
            | * Function whose KDoc includes a plain kotlin code sample:
            | * ```
            | * val x = 1
            | * ```
            | */
            |fun withCodeSample(): String = "k"
        """.trimIndent()

        testInline(
            source,
            configuration,
            pluginOverrides = listOf(writerPlugin, ComposePreviewLabDokkaPlugin()),
        ) {
            renderingStage = { _, _ ->
                val html = writerPlugin.writer.contents.getValue("root/example/with-code-sample.html")
                val doc = Jsoup.parse(html)
                doc.select("iframe.preview-lab-embedded").shouldHaveSize(0)
                doc.select("div.sample-container").size shouldBe 1
            }
        }
    }
}
