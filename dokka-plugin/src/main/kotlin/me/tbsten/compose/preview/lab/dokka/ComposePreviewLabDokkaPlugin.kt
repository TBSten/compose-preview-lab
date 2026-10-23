package me.tbsten.compose.preview.lab.dokka

import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement

/**
 * Dokka plugin that embeds Compose Preview Lab previews into generated KDoc pages.
 *
 * Write `@previewLab <previewId> [WxH]` in a KDoc comment:
 *
 * ```kotlin
 * /**
 *  * A primary button.
 *  *
 *  * @previewLab MyButtonPreview 1280x680
 *  */
 * @Composable
 * fun MyButton(onClick: () -> Unit) { /* … */ }
 * ```
 *
 * and the generated HTML page of `MyButton` gets an `<iframe>` that loads
 * `<baseUrl>/?iframe&previewId=MyButtonPreview` – the URL convention understood by
 * `EmbeddedPreviewOrGallery` on the hosted gallery site. `previewId` is the id of the `@Preview`
 * (`@ComposePreviewLabOption(id = …)`, `{{qualifiedName}}` by default) and the optional `WxH` becomes
 * the iframe's CSS `aspect-ratio`.
 *
 * Where the gallery lives and how the iframe looks is configured through
 * [ComposePreviewLabDokkaConfiguration] (Dokka `pluginsConfiguration`).
 *
 * The plugin is picked up by Dokka through the `META-INF/services/org.jetbrains.dokka.plugability.DokkaPlugin`
 * entry; add it with `dokkaPlugin("me.tbsten.compose.preview.lab:dokka-plugin:<version>")`.
 */
public class ComposePreviewLabDokkaPlugin : DokkaPlugin() {
    private val dokkaBase by lazy { plugin<DokkaBase>() }

    /** Replaces Dokka's HTML renderer with [PreviewLabHtmlRenderer] (iframe rendering of the `@previewLab` block). */
    public val previewLabRenderer by extending {
        CoreExtensions.renderer providing { PreviewLabHtmlRenderer(it) } override dokkaBase.htmlRenderer
    }

    /** Teaches Dokka's documentable → page translation about the `@previewLab` KDoc tag. */
    public val previewLabTagContentProvider by extending {
        dokkaBase.customTagContentProvider with PreviewLabTagContentProvider order {
            before(dokkaBase.sinceKotlinTagContentProvider)
        }
    }

    @OptIn(DokkaPluginApiPreview::class)
    override fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement = PluginApiPreviewAcknowledgement
}
