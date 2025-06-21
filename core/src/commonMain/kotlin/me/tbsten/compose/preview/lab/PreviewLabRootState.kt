package me.tbsten.compose.preview.lab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PreviewLabRootState(selectedPreview: CollectedPreview? = null,) {
    var selectedPreview: CollectedPreview? by mutableStateOf(selectedPreview)
        private set

    fun select(preview: CollectedPreview) {
        selectedPreview = preview
    }

    fun unselect() {
        selectedPreview = null
    }
}
