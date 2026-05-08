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
 * ### About `collectScope`
 *
 * `collectScope` declares the "buckets" under which this preview is collected. Each entry
 * becomes a key for `collectModulePreviews(scope = "...")` /
 * `collectAllModulePreviews(scope = "...")` on the consumer side, so a preview with
 * `collectScope = ["design", "showcase"]` is picked up by both
 * `collectAllModulePreviews(scope = "design")` and
 * `collectAllModulePreviews(scope = "showcase")` — one preview, multiple galleries — without
 * any duplication on the source side.
 *
 * ```kt
 * @ComposePreviewLabOption(collectScope = ["design", "showcase"])
 * @Preview
 * @Composable
 * fun ButtonShowcase() { MyButton() }
 *
 * // app side:
 * val designGallery by collectAllModulePreviews(scope = "design")     // sees ButtonShowcase
 * val showcaseGallery by collectAllModulePreviews(scope = "showcase") // also sees ButtonShowcase
 * val defaultGallery by collectAllModulePreviews()                    // does NOT see it
 * ```
 *
 * Each entry must match `[A-Za-z0-9_]+` because the compiler plugin embeds it into the
 * synthetic hint function name (`previewHint_<scope>`); anything else is reported as a
 * compile error. The default `["default"]` makes previews without an explicit override
 * flow through the corresponding default-scope `collect[All]ModulePreviews()` call
 * automatically. `ignore = true` always wins, regardless of how many scopes are listed.
 *
 * @property displayName {{package}}, {{simpleName}}, {{qualifiedName}} or a custom string. It does not have to match other Previews as it does not function like an ID. `. Each segment separated by ` is considered a group.
 * @property ignore if true, Compose Preview Lab does not collect this Preview. The exclusion applies in both directions: the same module's `collectModulePreviews()` drops it via the IR-side filter, and consumer modules' `collectAllModulePreviews()` cannot discover it because no per-declaration hint is emitted in the first place.
 * @property id An ID to identify each Preview. It can be used for navigation within the PreviewLabNavController. The same placeholder as displayName can be used.
 * @property collectScope The collection scopes this preview participates in. See the *About `collectScope`* section above for the semantics, the validation rules, and a multi-scope example.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class ComposePreviewLabOption(
    val displayName: String = "{{qualifiedName}}",
    val ignore: Boolean = false,
    val id: String = "{{qualifiedName}}",
    val collectScope: Array<String> = ["default"],
)
