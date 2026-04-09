package me.tbsten.compose.preview.lab

/**
 * Test stub for CollectedPreview.
 * The real class (in the core module) uses `@Composable () -> Unit` for `content`,
 * but for compiler plugin tests we use `() -> Unit` to avoid a Compose runtime dependency.
 */
data class CollectedPreview(
    val id: String,
    val displayName: String = id,
    val filePath: String? = null,
    val startLineNumber: Int? = null,
    val endLineNumber: Int? = null,
    val code: String? = null,
    val kdoc: String? = null,
    val content: () -> Unit = {},
)
