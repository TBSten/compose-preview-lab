package me.tbsten.compose.preview.lab.dokka

import org.jetbrains.dokka.plugability.ConfigurableBlock

/**
 * Typed configuration block of [ComposePreviewLabDokkaPlugin].
 *
 * Dokka hands plugin configuration over as JSON (`pluginsConfiguration`), and this class is what that JSON
 * is deserialised into. Every property has a default, so consumers only need to spell out what they want
 * to change:
 *
 * ```json
 * {
 *   "baseUrl": "https://example.com/compose-preview-lab-gallery",
 *   "iframe": { "defaultHeight": "600px", "border": "1px solid #ccc" }
 * }
 * ```
 *
 * With the Dokka Gradle plugin (v2) and the `me.tbsten.compose.preview.lab` Gradle plugin applied to the
 * same module, the same configuration is expressed type-safely (no import needed):
 *
 * ```kotlin
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
 * @property baseUrl URL of the hosted Compose Preview Lab gallery (the site rendering
 *   `EmbeddedPreviewOrGallery`). `?iframe&previewId=<id>` is appended to it. Absolute values
 *   (`https://…`, `//…`, `/…`) are used verbatim; a relative value is resolved against the **root of the
 *   generated Dokka site**, so the default ([DefaultBaseUrl]) expects the gallery to be deployed next to
 *   Dokka's `index.html`.
 * @property iframe Inline style applied to every embedded iframe. See [IframeStyle].
 */
public data class ComposePreviewLabDokkaConfiguration(
    val baseUrl: String = DefaultBaseUrl,
    val iframe: IframeStyle = IframeStyle(),
) : ConfigurableBlock {
    /**
     * Inline CSS applied to the embedded `<iframe>` and its wrapper `<div class="preview-lab-embedded-container">`.
     *
     * @property defaultHeight CSS height used when the `@previewLab` tag does not specify a `WxH` aspect ratio.
     * @property border CSS `border` of the iframe.
     * @property borderRadius CSS `border-radius` of the iframe.
     * @property margin Space around the iframe. Applied as `padding` of the wrapper so the iframe itself can keep
     *   `width: 100%` without overflowing its column.
     * @property backgroundColor CSS `background-color` of the iframe, visible until the gallery has loaded.
     */
    public data class IframeStyle(
        val defaultHeight: String = DefaultHeight,
        val border: String = DefaultBorder,
        val borderRadius: String = DefaultBorderRadius,
        val margin: String = DefaultMargin,
        val backgroundColor: String = DefaultBackgroundColor,
    ) {
        public companion object {
            public const val DefaultHeight: String = "720px"
            public const val DefaultBorder: String = "2px solid #d0d7de"
            public const val DefaultBorderRadius: String = "0"
            public const val DefaultMargin: String = "16px"
            public const val DefaultBackgroundColor: String = "transparent"
        }
    }

    public companion object {
        /** Default [baseUrl]: a `compose-preview-lab-gallery/` directory next to the generated `index.html`. */
        public const val DefaultBaseUrl: String = "compose-preview-lab-gallery"
    }
}
