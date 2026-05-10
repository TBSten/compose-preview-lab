package sample

import me.tbsten.compose.preview.lab.CollectedPreview
import me.tbsten.compose.preview.lab.collectAllModulePreviews

private val samplePreviewsSequence by collectAllModulePreviews()

/**
 * Materializes the aggregated `@Preview` sequence once at module load and exposes the
 * resulting `List<CollectedPreview>`. Sample apps wire this list straight into the
 * Gallery, and the materialization gives them a stable snapshot instead of
 * re-iterating the sequence (and re-allocating each `@Composable` lambda) on every
 * call site.
 */
val samplePreviews: List<CollectedPreview> = samplePreviewsSequence.toList()
