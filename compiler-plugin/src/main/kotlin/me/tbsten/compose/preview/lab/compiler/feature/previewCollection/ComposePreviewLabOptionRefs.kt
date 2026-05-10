package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

// Identifiers for the user-facing `@ComposePreviewLabOption` annotation that augments
// `@Preview` with plugin-specific behaviour (`ignore`, `collectScopes`, ...).
//
// Both the FIR predicate registration (`PreviewAnnotationPredicates.optionPredicate`)
// and the annotation readers (`scopeValidation/` checkers + the `ignore` / `collectScopes`
// readers used by `HintEntriesProvider`) reach for these — keeping the constants at the
// feature root prevents drift between the predicate side (FQN string) and the readout
// side (`ClassId` + argument `Name`s).

/**
 * `me.tbsten.compose.preview.lab.ComposePreviewLabOption` FQN.
 *
 * Used both for FIR `LookupPredicate` registration (eagerly resolves the annotation
 * type ref on every `@Preview` symbol) and as the source of
 * [COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID].
 */
internal val COMPOSE_PREVIEW_LAB_OPTION_FQN: FqName =
    FqName("me.tbsten.compose.preview.lab.ComposePreviewLabOption")

/**
 * `me.tbsten.compose.preview.lab.ComposePreviewLabOption` `ClassId` — looked up on each
 * `@Preview` symbol to read user-supplied options (e.g. `ignore = true`) during hint
 * emission.
 */
internal val COMPOSE_PREVIEW_LAB_OPTION_CLASS_ID: ClassId =
    ClassId.topLevel(COMPOSE_PREVIEW_LAB_OPTION_FQN)

/** `@ComposePreviewLabOption(ignore = ...)` argument name. */
internal val IGNORE_NAME: Name = Name.identifier("ignore")

/** `@ComposePreviewLabOption(collectScopes = ...)` argument name. */
internal val COLLECT_SCOPE_NAME: Name = Name.identifier("collectScopes")
