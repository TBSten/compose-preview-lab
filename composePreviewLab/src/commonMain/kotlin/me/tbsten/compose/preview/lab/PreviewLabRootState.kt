package me.tbsten.compose.preview.lab

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class PreviewLabRootState {
    var selectedPreviewIndex by mutableIntStateOf(0)
}
