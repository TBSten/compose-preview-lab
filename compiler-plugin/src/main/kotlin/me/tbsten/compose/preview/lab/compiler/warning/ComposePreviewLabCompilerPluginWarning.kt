package me.tbsten.compose.preview.lab.compiler.warning

import me.tbsten.compose.preview.lab.compiler.error.ComposePreviewLabCompilerPluginError

/**
 * Structured representation of a warning reported by the compose-preview-lab compiler
 * plugin. Mirrors [ComposePreviewLabCompilerPluginError] in shape, so the rendering
 * code in `WarningContextDsl.kt` / `ReportWarning.kt` can be kept symmetric to the
 * error side without duplicating fields.
 *
 * Ticket 0 only ships the skeleton — concrete warning classes are added in Ticket 4
 * when the existing `ir/HintDiscovery.kt` squatting-guard and cross-artifact-dup
 * warnings are migrated.
 */
interface ComposePreviewLabCompilerPluginWarning {
    /** See [ComposePreviewLabCompilerPluginError.categories]. */
    val categories: List<Category>

    /** Single-line summary printed right after the `[ComposePreviewLab/<categories>]` prefix. */
    val message: String

    /** Optional multi-line static explanation. */
    val description: String? get() = null

    /**
     * Ordered list of pre-rendered context lines (see
     * [ComposePreviewLabCompilerPluginError.context]). Build with the warning-side
     * [contextOf] DSL.
     */
    val context: List<String> get() = emptyList()

    /** Ordered list of human-readable next-action suggestions. */
    val replies: List<String>
}

/**
 * Category axis for warnings. Re-exports the error-side enum directly so a single
 * feature-axis vocabulary (`PREVIEW_COLLECTION`, `INVALID_USAGE`, ...) is shared by
 * both error and warning reporters — preventing drift between the two.
 */
typealias Category = ComposePreviewLabCompilerPluginError.Category
