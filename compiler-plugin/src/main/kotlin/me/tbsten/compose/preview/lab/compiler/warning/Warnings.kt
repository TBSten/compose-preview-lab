package me.tbsten.compose.preview.lab.compiler.warning

/**
 * Placeholder home for concrete [ComposePreviewLabCompilerPluginWarning] implementations.
 *
 * Ticket 0 only ships the warning skeleton (interface + DSL + reporter). The two
 * existing warning sites in
 * `feature/previewCollection/ir/collectPreviewsReplacement/DiscoverHints.kt` (the
 * squatting guard near line 154 and the cross-artifact dup detection near line 174)
 * are migrated in Ticket 4 along with any new warning classes introduced by the
 * warning-expansion plan.
 *
 * The empty object keeps the file layout symmetric to `error/Errors.kt` so future
 * warnings have an obvious place to land.
 */
internal object Warnings
