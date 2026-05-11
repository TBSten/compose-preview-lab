package me.tbsten.compose.preview.lab.gallery

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.js.ExperimentalJsExport
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.util.JsOnlyExport
import me.tbsten.compose.preview.lab.util.JsOnlyExportIgnore

/**
 * State holder for [PreviewLabGallery].
 *
 * @param initialSelectedPreview Preview, selected by default.
 *
 * @see PreviewLabGallery
 */
@OptIn(ExperimentalJsExport::class)
@JsOnlyExport
@Stable
class PreviewLabGalleryState @JsOnlyExportIgnore constructor(initialSelectedPreview: Pair<String, PreviewLabPreview>? = null) {
    internal var selectedPreview: SelectedPreview? by mutableStateOf(
        initialSelectedPreview
            ?.let(SelectedPreview::from),
    )
        private set

    internal var comparePanelPreviews = mutableStateListOf<SelectedPreview>()

    @InternalComposePreviewLabApi
    val selectedPreviews: List<SelectedPreview> by derivedStateOf {
        selectedPreview?.let { selectedPreview ->
            listOf(selectedPreview) + comparePanelPreviews
        } ?: emptyList()
    }

    internal val canAddToComparePanel by derivedStateOf { selectedPreviews.isNotEmpty() }

    var query by mutableStateOf("")
        @InternalComposePreviewLabApi set

    fun onQueryChange(query: String) {
        this.query = query
    }

    @JsOnlyExportIgnore
    fun select(groupName: String, preview: PreviewLabPreview) {
        val newSelectedPreview = SelectedPreview(groupName, preview)
        if (selectedPreview == newSelectedPreview) {
            unselect()
        } else {
            selectedPreview = SelectedPreview(groupName = groupName, preview = preview)
        }
    }

    /**
     * Deselects the current preview and clears the compare panel (= "nothing selected" state).
     */
    fun unselect() {
        selectedPreview = null
        comparePanelPreviews.clear()
    }

    /**
     * Adds a preview to the compare panel. If a preview with the same display name is already
     * panelled, a `(n)` suffix is appended for distinction. The dedupe loop is capped at 100
     * iterations as a safety guard against pathologically duplicated names.
     */
    @JsOnlyExportIgnore
    fun addToComparePanel(groupName: String, newPreview: PreviewLabPreview) {
        val newPanelTitle = run {
            val baseTitle = newPreview.displayName
            var newPanelTitle = baseTitle
            var count = 1
            while (selectedPreviews.any { it.title == newPanelTitle }) {
                newPanelTitle = "$baseTitle ($count)"
                count++
                // 無限ループ対策 (同じ名前の Preview が多数ある場合)
                // 通常は起こりえないはず
                if (count > 100) break
            }
            newPanelTitle
        }
        comparePanelPreviews.add(SelectedPreview(groupName, newPreview, title = newPanelTitle))
    }

    /**
     * Removes a panelled preview by its index in [selectedPreviews]. Index 0 is the main
     * selected preview (use [unselect] for that one); index >=1 maps to the compare panel.
     */
    fun removeFromComparePanel(indexInSelectedPreviews: Int) {
        val indexInComparePanelPreviews = indexInSelectedPreviews - 1
        comparePanelPreviews.removeAt(indexInComparePanelPreviews)
    }
}

@InternalComposePreviewLabApi
class SelectedPreview(val groupName: String, val preview: PreviewLabPreview, val title: String = preview.displayName) {
    override fun toString(): String = "SelectedPreview(groupName='$groupName', preview=$preview)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SelectedPreview) return false

        if (groupName != other.groupName) return false
        if (preview != other.preview) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groupName.hashCode()
        result = 31 * result + preview.hashCode()
        return result
    }

    companion object {
        fun from(preview: Pair<String, PreviewLabPreview>) = SelectedPreview(
            groupName = preview.first,
            preview = preview.second,
        )
    }
}
