package me.tbsten.compose.preview.lab

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * State holder for [PreviewLabRoot].
 *
 * @param initialSelectedPreview 初期状態で選択されている Preview。
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
