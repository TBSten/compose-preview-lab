package me.tbsten.compose.preview.lab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PreviewLabRootState {
    var selectedPreview: CollectedPreview? by mutableStateOf(null)
}
