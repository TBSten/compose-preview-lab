package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

// Single source of truth for the marker interface short name
// (`PreviewHintMarker_<sanitized_fqn>_<hash>`).
//
// Three call-sites participate:
// - Generation side (FIR `GeneratePreviewHintMarkerFir`) emits the marker class.
// - Restoration side (IR `FillPreviewHintIrBody`) recovers the hash from the marker
//   parameter type's short name via `extractHashFromMarkerShortName`.
// - Discovery side (IR `DiscoverHints`) accepts only marker classes whose short name
//   matches `isMarkerShortName(...)` (= structural sanity check before consulting the
//   `@SyntheticPreviewHint` annotation).
//
// All three go through this file so the format never drifts.

/**
 * Prefix of the per-`@Preview` marker interface name. Full name is
 * `PreviewHintMarker_<sanitized_fqn>_<hash>` where `<sanitized_fqn>` is the source
 * FQN with `.` replaced by `_` (debugging aid) and `<hash>` is the canonical-key
 * sha256 ([HashLength] chars, used for overload disambiguation and as the
 * cross-FIR/IR matching key).
 */
internal const val PreviewHintMarkerPrefix: String = "PreviewHintMarker_"

/** Length of the hash suffix on marker / hint declarations (matches `computeHintHash`). */
internal const val HashLength: Int = 8

/** Characters that are not legal in a non-backticked Kotlin identifier. */
private val NonIdentifierCharRegex = Regex("[^A-Za-z0-9_]")

/**
 * Builds the marker interface short name `PreviewHintMarker_<sanitized_fqn>_<hash>`.
 *
 * The sanitized FQN replaces every character outside `[A-Za-z0-9_]` with `_`. This is a
 * debugging aid that makes IDE navigation, stack traces, and KLIB IC logs immediately
 * reveal which `@Preview` a marker belongs to. A naive `.` → `_` substitution is not
 * enough because backtick-quoted identifiers (e.g. `` fun `my preview`() ``) are valid in
 * Kotlin source but contain characters that `Name.identifier(...)` would reject, so we
 * sanitize comprehensively here.
 *
 * The hash suffix disambiguates same-name overloads (the sha256 of
 * [buildPreviewHintCanonicalKey]); any information loss from sanitization (e.g. `A.B` and
 * `A_B` mapping to the same sanitized form) is absorbed by the hash.
 *
 * **Sample**: `buildMarkerShortName("uilib.button.MyButton", "a3k9z2x1")`
 * → `"PreviewHintMarker_uilib_button_MyButton_a3k9z2x1"`
 *
 * **Sample (identifier-illegal characters)**: `buildMarkerShortName("uilib.`my preview`", "h4sh1234")`
 * → `"PreviewHintMarker_uilib__my_preview__h4sh1234"`
 */
internal fun buildMarkerShortName(sourceFqn: String, hash: String): String {
    val sanitizedFqn = sourceFqn.replace(NonIdentifierCharRegex, "_")
    return "$PreviewHintMarkerPrefix${sanitizedFqn}_$hash"
}

/**
 * Whether [shortName] is the synthesized marker interface pattern (= starts with
 * `PreviewHintMarker_`). A pure structural check; combine with the
 * `@SyntheticPreviewHint` annotation when authenticity proof is required.
 *
 * **Sample**:
 * - `isMarkerShortName("PreviewHintMarker_uilib_MyButton_a3k9z2x1")` → `true`
 * - `isMarkerShortName("MyOwnInterface")` → `false`
 */
internal fun isMarkerShortName(shortName: String): Boolean = shortName.startsWith(PreviewHintMarkerPrefix)

/**
 * Recovers the hash suffix from a marker class short name.
 *
 * The marker name format is `PreviewHintMarker_<sanitized_fqn>_<hash>`. The hash is a
 * fixed-length ([HashLength]) base-36 string, so a tail slice recovers it without
 * parsing the sanitized portion (which is lossy).
 *
 * **Sample**: `extractHashFromMarkerShortName("PreviewHintMarker_uilib_MyButton_a3k9z2x1")` → `"a3k9z2x1"`
 *
 * Caller is responsible for first ensuring [isMarkerShortName] holds — passing a
 * non-marker name returns its trailing 8 characters silently.
 */
internal fun extractHashFromMarkerShortName(shortName: String): String = shortName.takeLast(HashLength)
