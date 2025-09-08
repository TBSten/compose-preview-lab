package me.tbsten.compose.preview.lab

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * State holder for [PreviewLabRoot].
 *
 * @param initialSelectedPreview Preview, selected by default.
 *
 * @see PreviewLabRoot
 */
class PreviewLabRootState(initialSelectedPreview: Pair<String, CollectedPreview>? = null) {
    internal var selectedPreview: SelectedPreview? by mutableStateOf(
        initialSelectedPreview
            ?.let(SelectedPreview::from),
    )
        private set

    internal var comparePanelPreviews = mutableStateListOf<SelectedPreview>()

    internal val selectedPreviews: List<SelectedPreview> by derivedStateOf {
        selectedPreview?.let { selectedPreview ->
            listOf(selectedPreview) + comparePanelPreviews
        } ?: emptyList()
    }

    internal val canAddToComparePanel by derivedStateOf { selectedPreviews.isNotEmpty() }

    var query by mutableStateOf("")
        private set

    /**
     * Changes the search query
     *
     * Updates the search query string used for filtering Previews.
     * This value is used by the filtering functionality to narrow down the Preview list.
     *
     * @param query New search query string
     */
    fun onQueryChange(query: String) {
        this.query = query
    }

    /**
     * Select Preview.
     */
    fun select(groupName: String, preview: CollectedPreview) {
        val newSelectedPreview = SelectedPreview(groupName, preview)
        if (selectedPreview == newSelectedPreview) {
            unselect()
        } else {
            selectedPreview = SelectedPreview(groupName = groupName, preview = preview)
        }
    }

    /**
     * Deselects the selected Preview and returns it to the "nothing selected" state.
     */
    fun unselect() {
        selectedPreview = null
        comparePanelPreviews.clear()
    }

    /**
     * Adds a Preview to the compare panel
     *
     * In addition to the already selected Preview, adds a new Preview to the compare panel.
     * If a Preview with the same name already exists, a sequentially numbered title is
     * automatically assigned for distinction. Supports up to 100 Previews with the same name
     * to prevent infinite loops.
     *
     * @param groupName Group name the Preview belongs to
     * @param newPreview Preview to add to the compare panel
     */
    fun addToComparePanel(groupName: String, newPreview: CollectedPreview) {
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
     * Removes a Preview from the compare panel
     *
     * Specifies an index in the selected Preview list to remove the corresponding
     * Preview from the compare panel. Index 0 is the main selected Preview,
     * and index 1 and above are Previews in the compare panel.
     *
     * @param indexInSelectedPreviews Index position in the selected Preview list (1 or greater)
     */
    fun removeFromComparePanel(indexInSelectedPreviews: Int) {
        val indexInComparePanelPreviews = indexInSelectedPreviews - 1
        comparePanelPreviews.removeAt(indexInComparePanelPreviews)
    }
}

internal class SelectedPreview(val groupName: String, val preview: CollectedPreview, val title: String = preview.displayName) {
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
        fun from(preview: Pair<String, CollectedPreview>) = SelectedPreview(
            groupName = preview.first,
            preview = preview.second,
        )
    }
}
