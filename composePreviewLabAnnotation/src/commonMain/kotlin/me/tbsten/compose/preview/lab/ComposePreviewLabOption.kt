package me.tbsten.compose.preview.lab

/**
 *
 * ### usages
 *
 * By default, the qualifiedName of the Preview (ex. com.example.my.buttons.MyButtonPreview) is used as the Preview name, but this can be overridden by displayName.
 * The name `MyButtonPreview` can be displayed on ComposePreviewLab by specifying the following.
 *
 * ```kt
 * package com.example.my.buttons
 *
 * @ComposePreviewLabOption(displayName = "{{simpleName}}")
 * @Preview
 * @Composable
 * fun MyButtonPreview() {
 *     MyButton()
 * }
 * ```
 *
 * @property displayName {{package}}, {{simpleName}}, {{qualifiedName}} or a custom string. It does not have to match other Previews as it does not function like an ID.
 * @property ignore if true, Compose Preview Lab Gradle Plugin don't collect this Preview.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class ComposePreviewLabOption(
    val displayName: String = "{{qualifiedName}}",
    val ignore: Boolean = false,
)
