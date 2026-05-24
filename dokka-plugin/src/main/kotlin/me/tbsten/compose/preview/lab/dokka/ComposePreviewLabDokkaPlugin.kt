package me.tbsten.compose.preview.lab.dokka

import org.jetbrains.dokka.CoreExtensions
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.DokkaPluginApiPreview
import org.jetbrains.dokka.plugability.PluginApiPreviewAcknowledgement

public class ComposePreviewLabDokkaPlugin : DokkaPlugin() {
    private val dokkaBase by lazy { plugin<DokkaBase>() }

    public val previewLabRenderer by extending {
        CoreExtensions.renderer providing { PreviewLabHtmlRenderer(it) } override dokkaBase.htmlRenderer
    }

    public val previewLabTagContentProvider by extending {
        dokkaBase.customTagContentProvider with PreviewLabTagContentProvider order {
            before(dokkaBase.sinceKotlinTagContentProvider)
        }
    }

    @OptIn(DokkaPluginApiPreview::class)
    override fun pluginApiPreviewAcknowledgement(): PluginApiPreviewAcknowledgement = PluginApiPreviewAcknowledgement
}
