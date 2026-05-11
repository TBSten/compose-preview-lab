package me.tbsten.compose.preview.lab

/**
 * Internal — marks a module so the compiler plugin includes its previews in the aggregated
 * `PreviewAllList`. Attached automatically; do not apply manually.
 */
@InternalComposePreviewLabApi
annotation class AggregateToAll
