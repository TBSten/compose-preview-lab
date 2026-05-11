package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import me.tbsten.compose.preview.lab.compiler.utils.classIdOf
import org.jetbrains.kotlin.name.ClassId

/**
 * `me.tbsten.compose.preview.lab.CollectedPreview` `ClassId`.
 *
 * Used as the return type of every synthesized hint function
 * (`previewHint_<scope>(value: ...): CollectedPreview`) and as the constructor target
 * when the IR pass fills in hint bodies / builds `lazyPreviewSequence({...})` factories.
 *
 * Lives at the feature root because both the FIR-side hint generator
 * (`hintAndMarkerGeneration/PreviewHintFirGenerator`) and the IR-side
 * `CollectedPreview(...)` builder (`buildPreviewSequence/BuildCollectedPreviewIr`) need it.
 */
internal val COLLECTED_PREVIEW_CLASS_ID: ClassId = classIdOf("me.tbsten.compose.preview.lab", "CollectedPreview")
