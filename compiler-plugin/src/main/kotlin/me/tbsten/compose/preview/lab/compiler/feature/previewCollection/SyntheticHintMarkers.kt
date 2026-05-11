package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import me.tbsten.compose.preview.lab.compiler.utils.classIdOf
import org.jetbrains.kotlin.name.ClassId

/**
 * `me.tbsten.compose.preview.lab.InternalComposePreviewLabApi` `ClassId`.
 *
 * Attached by `fir/AttachInternalApi.markAsInternalSyntheticHint` to every synthesized
 * marker interface and hint function so the apiValidation `nonPublicMarkers` filter
 * keeps them out of the BCV baseline on every CMP target.
 */
internal val INTERNAL_COMPOSE_PREVIEW_LAB_API_CLASS_ID: ClassId =
    classIdOf("me.tbsten.compose.preview.lab", "InternalComposePreviewLabApi")

/**
 * `me.tbsten.compose.preview.lab.SyntheticPreviewHint` `ClassId`.
 *
 * Stamped by `fir/AttachInternalApi.markAsInternalSyntheticHint` onto every synthesized
 * marker / hint declaration; the IR-side `DiscoverHints` cross-checks the annotation as
 * positive proof of authenticity (third-party code living in the
 * `me.tbsten.compose.preview.lab.hints` package without this marker is rejected with a
 * compile-time warning).
 */
internal val SYNTHETIC_PREVIEW_HINT_CLASS_ID: ClassId =
    classIdOf("me.tbsten.compose.preview.lab", "SyntheticPreviewHint")
