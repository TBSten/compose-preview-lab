package me.tbsten.compose.preview.lab.dokka

import javax.inject.Inject
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.jetbrains.dokka.gradle.engine.plugins.DokkaPluginParametersBaseSpec
import org.jetbrains.dokka.gradle.internal.InternalDokkaGradlePluginApi

/**
 * Type-safe `pluginsConfiguration` entry for the Compose Preview Lab Dokka plugin
 * (`me.tbsten.compose.preview.lab:dokka-plugin`).
 *
 * The `me.tbsten.compose.preview.lab` Gradle plugin registers one instance, named [Name], in
 * `dokka.pluginsConfiguration` as soon as the Dokka Gradle plugin (v2) is applied to the same project.
 * Gradle then generates a `composePreviewLab { }` accessor, so a build script configures the Dokka plugin
 * without any import:
 *
 * ```kotlin
 * plugins {
 *     id("org.jetbrains.dokka")
 *     id("me.tbsten.compose.preview.lab")
 * }
 *
 * dependencies {
 *     dokkaPlugin("me.tbsten.compose.preview.lab:dokka-plugin:<version>")
 * }
 *
 * dokka {
 *     pluginsConfiguration {
 *         composePreviewLab {
 *             baseUrl = "https://example.com/compose-preview-lab-gallery"
 *             iframe {
 *                 defaultHeight = "600px"
 *                 border = "1px solid #ccc"
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * Only properties that are explicitly set are forwarded; everything else keeps the Dokka plugin's own
 * defaults. [jsonEncode] mirrors `me.tbsten.compose.preview.lab.dokka.ComposePreviewLabDokkaConfiguration`
 * of the `dokka-plugin` artifact.
 */
// The `DokkaPluginParametersBaseSpec` constructor is the documented extension point for third-party
// plugin parameters, but Dokka still marks it as internal API.
@OptIn(InternalDokkaGradlePluginApi::class)
abstract class ComposePreviewLabDokkaPluginParameters @Inject constructor(name: String) :
    DokkaPluginParametersBaseSpec(name, PluginFqn) {
    /**
     * URL of the hosted Compose Preview Lab gallery (the site rendering `EmbeddedPreviewOrGallery`).
     * `?iframe&previewId=<id>` is appended to it.
     *
     * Absolute values (`https://…`, `//…`, `/…`) are used verbatim. A relative value is resolved against
     * the root of the generated Dokka site, so the default (`compose-preview-lab-gallery`) expects the gallery
     * to be deployed next to Dokka's `index.html`.
     */
    @get:Input
    @get:Optional
    abstract val baseUrl: Property<String>

    /** Inline style of the embedded iframe. See [ComposePreviewLabDokkaIframeStyle]. */
    @get:Nested
    abstract val iframe: ComposePreviewLabDokkaIframeStyle

    /** Kotlin/Groovy DSL friendly accessor for [iframe]. */
    fun iframe(action: Action<in ComposePreviewLabDokkaIframeStyle>) {
        action.execute(iframe)
    }

    override fun jsonEncode(): String {
        val iframeJson = jsonObject(
            "defaultHeight" to iframe.defaultHeight.orNull?.let(::jsonString),
            "border" to iframe.border.orNull?.let(::jsonString),
            "borderRadius" to iframe.borderRadius.orNull?.let(::jsonString),
            "margin" to iframe.margin.orNull?.let(::jsonString),
            "backgroundColor" to iframe.backgroundColor.orNull?.let(::jsonString),
        )
        return jsonObject(
            "baseUrl" to baseUrl.orNull?.let(::jsonString),
            "iframe" to iframeJson.takeIf { it != EmptyJsonObject },
        )
    }

    companion object {
        /** Name of the entry in `dokka.pluginsConfiguration`. */
        const val Name: String = "composePreviewLab"

        /** Fully qualified name of the Dokka plugin class in the `dokka-plugin` artifact. */
        const val PluginFqn: String = "me.tbsten.compose.preview.lab.dokka.ComposePreviewLabDokkaPlugin"
    }
}

/**
 * Inline CSS applied to the `<iframe>` embedded by the Compose Preview Lab Dokka plugin.
 *
 * Unset properties keep the plugin defaults
 * (`720px` / `2px solid #d0d7de` / `0` / `16px` / `transparent`).
 */
abstract class ComposePreviewLabDokkaIframeStyle {
    /** CSS height used when the `@previewLab` tag does not specify a `WxH` aspect ratio. */
    @get:Input
    @get:Optional
    abstract val defaultHeight: Property<String>

    /** CSS `border` of the iframe. */
    @get:Input
    @get:Optional
    abstract val border: Property<String>

    /** CSS `border-radius` of the iframe. */
    @get:Input
    @get:Optional
    abstract val borderRadius: Property<String>

    /** Space around the iframe (applied as `padding` of the wrapper element). */
    @get:Input
    @get:Optional
    abstract val margin: Property<String>

    /** CSS `background-color` of the iframe, visible until the gallery has loaded. */
    @get:Input
    @get:Optional
    abstract val backgroundColor: Property<String>
}

private const val EmptyJsonObject = "{}"

// Minimal JSON helpers: the values are build-author supplied CSS snippets / URLs, so a hand-rolled
// encoder keeps the Gradle plugin free of a JSON library dependency.
private fun jsonObject(vararg entries: Pair<String, String?>): String = entries
    .filter { (_, encodedValue) -> encodedValue != null }
    .joinToString(separator = ",", prefix = "{", postfix = "}") { (key, encodedValue) ->
        "${jsonString(key)}:$encodedValue"
    }

private fun jsonString(value: String): String = buildString {
    append('"')
    value.forEach { ch ->
        when {
            ch == '"' -> append("\\\"")
            ch == '\\' -> append("\\\\")
            ch == '\n' -> append("\\n")
            ch == '\r' -> append("\\r")
            ch == '\t' -> append("\\t")
            ch < ' ' -> append("\\u%04x".format(ch.code))
            else -> append(ch)
        }
    }
    append('"')
}
