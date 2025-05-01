package me.tbsten.compose.preview.lab.me.component

import androidx.compose.ui.tooling.preview.Preview

@Suppress("ComposePreviewDimensionRespectsLimit")
@Preview(
    widthDp = LabPreview.Medium.WidthDp,
    heightDp = LabPreview.Medium.HeightDp,
)
annotation class LabPreview {
    @Preview(
        widthDp = Small.WidthDp,
        heightDp = Small.HeightDp,
    )
    annotation class Small {
        companion object {
            const val WidthDp = 200 + 300
            const val HeightDp = -1
        }
    }

    @Preview(
        widthDp = Medium.WidthDp,
        heightDp = Medium.HeightDp,
    )
    annotation class Medium {
        companion object {
            const val WidthDp = 500 + 300
            const val HeightDp = -1
        }
    }

    @Preview(
        widthDp = Large.WidthDp,
        heightDp = Large.HeightDp,
    )
    annotation class Large {
        companion object {
            const val WidthDp = 800 + 300
            const val HeightDp = -1
        }
    }
}
