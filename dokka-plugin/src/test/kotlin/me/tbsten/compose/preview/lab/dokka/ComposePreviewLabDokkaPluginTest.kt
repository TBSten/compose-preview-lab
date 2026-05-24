package me.tbsten.compose.preview.lab.dokka

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
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

    @Test
    fun `previewLab base URL can be overridden via system property`() {
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
