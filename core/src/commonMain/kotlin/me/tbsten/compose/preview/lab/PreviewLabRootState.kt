package me.tbsten.compose.preview.lab

import androidx.compose.runtime.getValue
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
    }
}

internal class SelectedPreview(val groupName: String, val preview: CollectedPreview) {
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
