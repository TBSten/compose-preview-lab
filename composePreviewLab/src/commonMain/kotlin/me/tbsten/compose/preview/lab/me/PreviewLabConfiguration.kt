package me.tbsten.compose.preview.lab.me

import androidx.compose.ui.unit.Dp

class PreviewLabConfiguration(
    val name: String,
    val maxWidth: Dp? = null,
    val maxHeight: Dp? = null,
) {
    override fun toString(): String =
        "PreviewLabConfiguration(name=$name, maxWidth=$maxWidth, maxHeight=$maxHeight)"

    companion object {
        val FitContent = PreviewLabConfiguration(
            name = "fit-content-size",
            maxWidth = null,
            maxHeight = null,
        )
        val Default = PreviewLabConfiguration(
            name = "<default>",
            maxWidth = null,
            maxHeight = null,
        )
    }
}
