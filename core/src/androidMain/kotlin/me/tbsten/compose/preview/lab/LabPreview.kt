package me.tbsten.compose.preview.lab

import androidx.compose.ui.tooling.preview.Preview

@Suppress("ComposePreviewDimensionRespectsLimit")
@Preview(
    widthDp = LabPreview.Medium.WidthDp,
    heightDp = LabPreview.Medium.HeightDp,
)
public annotation class LabPreview {
    @Preview(
        widthDp = Small.WidthDp,
        heightDp = Small.HeightDp,
    )
    public annotation class Small {
        public companion object {
            public const val WidthDp: Int = 200 + 300
            public const val HeightDp: Int = -1
        }
    }

    @Preview(
        widthDp = Medium.WidthDp,
        heightDp = Medium.HeightDp,
    )
    public annotation class Medium {
        public companion object {
            public const val WidthDp: Int = 500 + 300
            public const val HeightDp: Int = -1
        }
    }

    @Preview(
        widthDp = Large.WidthDp,
        heightDp = Large.HeightDp,
    )
    public annotation class Large {
        public companion object {
            public const val WidthDp: Int = 800 + 300
            public const val HeightDp: Int = -1
        }
    }
}
