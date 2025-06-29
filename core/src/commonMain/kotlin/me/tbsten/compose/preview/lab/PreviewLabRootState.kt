package me.tbsten.compose.preview.lab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * State holder for [PreviewLabRoot].
 *
 * @param selectedPreview 初期状態で選択されている Preview。
 * @property selectedPreview 選択されている Preview の情報を保持します。
 *
 * @see PreviewLabRoot
 */
class PreviewLabRootState(selectedPreview: CollectedPreview? = null) {
    var selectedPreview: CollectedPreview? by mutableStateOf(selectedPreview)
        private set

    var query by mutableStateOf("")
        private set

    fun onQueryChange(query: String) {
        this.query = query
    }

    /**
     * Select Preview.
     */
    fun select(preview: CollectedPreview) {
        selectedPreview = preview
    }

    /**
     * Deselects the selected Preview and returns it to the "nothing selected" state.
     */
    fun unselect() {
        selectedPreview = null
    }
}
