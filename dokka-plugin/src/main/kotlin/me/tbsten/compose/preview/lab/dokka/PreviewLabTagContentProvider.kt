package me.tbsten.compose.preview.lab.dokka

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.base.transformers.pages.tags.CustomTagContentProvider
import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder.DocumentableContentBuilder
import org.jetbrains.dokka.model.doc.CustomTagWrapper
import org.jetbrains.dokka.model.doc.P
import org.jetbrains.dokka.model.doc.Text

internal const val PreviewLabTag: String = "previewLab"
internal const val PreviewLabIframeCode: String = "@previewLab/iframe"

internal object PreviewLabTagContentProvider : CustomTagContentProvider {
    override fun isApplicable(customTag: CustomTagWrapper): Boolean = customTag.name == PreviewLabTag

    override fun DocumentableContentBuilder.contentForDescription(
        sourceSet: DokkaConfiguration.DokkaSourceSet,
        customTag: CustomTagWrapper,
    ) {
        val previewId = customTag.extractPreviewId() ?: return
        codeBlock(language = PreviewLabIframeCode) { text(previewId) }
    }

    private fun CustomTagWrapper.extractPreviewId(): String? {
        val firstParagraph = root.children.firstOrNull() as? P ?: return null
        val firstText = firstParagraph.children.firstOrNull() as? Text ?: return null
        return firstText.body.trim().takeIf { it.isNotEmpty() }
    }
}
