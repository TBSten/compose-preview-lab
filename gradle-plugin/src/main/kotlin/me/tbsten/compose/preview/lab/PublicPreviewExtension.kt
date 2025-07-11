package me.tbsten.compose.preview.lab

open class PublicPreviewExtension {
    var enabled: Boolean = true
    var annotations: List<String> = listOf(
        "androidx.compose.ui.tooling.preview.Preview",
        "org.jetbrains.compose.ui.tooling.preview.Preview",
    )
}
