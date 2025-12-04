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
 *
 * @ComposePreviewLabOption(displayName = "{{simpleName}}.Red")
 * @Preview
 * @Composable
 * fun MyButtonRedPreview() {
 *     MyButton(color = Color.Red)
 * }
 * ```
 *
 * Options such as displayName allow placeholders in the form `{{placeholderName}}`.
 * Supported placeholder meanings are as follows
 * - {{package}} ... the package name of the Preview, e.g. `com.example.my.buttons`.
 * - {{simpleName}} ... the simple name of the Preview, e.g. `MyButtonPreview`.
 * - {{qualifiedName}} ... the fully qualified name of the Preview, e.g. `com.example.my.buttons.MyButtonPreviewKt.MyButtonPreview` if the Preview is defined in a file named `MyButtonPreview.kt`.
 *
 * @property displayName {{package}}, {{simpleName}}, {{qualifiedName}} or a custom string. It does not have to match other Previews as it does not function like an ID. `. Each segment separated by ` is considered a group.
 * @property ignore if true, Compose Preview Lab Gradle Plugin don't collect this Preview.
 * @property id An ID to identify each Preview. It can be used for navigation within the PreviewLabNavController. The same placeholder as displayName can be used.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class ComposePreviewLabOption(
    val displayName: String = "{{qualifiedName}}",
    val ignore: Boolean = false,
    val id: String = "{{qualifiedName}}",
)
