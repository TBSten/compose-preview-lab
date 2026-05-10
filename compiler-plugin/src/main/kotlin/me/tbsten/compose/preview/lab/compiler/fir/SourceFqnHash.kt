package me.tbsten.compose.preview.lab.compiler.fir

import java.security.MessageDigest
import me.tbsten.compose.preview.lab.compiler.PreviewLabConstants

/**
 * Hash function that derives the per-declaration hint suffix from a `@Preview`'s
 * canonical signature key.
 *
 * Distinct from the per-module marker hash (`computeModuleHash`). In the per-declaration
 * design every `@Preview` produces exactly one hint, and the input is **only** the
 * canonical key. Including `projectRootPath` / `moduleName` would change the hint name
 * across build environments, breaking both incremental compilation and reproducible
 * builds, so neither is mixed in.
 *
 * Cross-artifact collisions on the same canonical key (e.g. two artifacts containing a
 * `@Preview` with identical FQN + signature) are an **accepted edge case to be resolved
 * by user-side namespace management**.
 *
 * **Sample call**: `computeHintHash("uiLib.button.MyButton()")`
 *
 * **Result**: an 8-char base-36 string such as `"a3k9z2x1"` (same format as
 * `computeModuleHash`).
 *
 * SHA-256 → first 8 bytes (64 bits) → base-36 encoding → last 8 characters. The
 * resulting alphabet has ~41 effective bits, which keeps the collision probability
 * negligibly small for practical namespace sizes.
 *
 * # Canonical key format
 *
 * To disambiguate same-name overloads (e.g. `fun MyButton()` and
 * `fun MyButton(text: String)` in the same package), callers must supply the canonical
 * key in the form `<sourceFqn>(<paramTypeFqns>)`:
 *
 * - sourceFqn: `<package>.<simpleName>`
 * - paramTypeFqns: parameter type FQNs joined by `,`. Nullability is suffixed with `?`
 *   (e.g. `kotlin.String?`).
 *
 * [buildPreviewHintCanonicalKey] is a helper that builds this canonical key from either
 * the FIR or IR APIs.
 */
internal fun computeHintHash(canonicalKey: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(canonicalKey.toByteArray(Charsets.UTF_8))
    val truncated = java.math.BigInteger(1, digest.copyOf(8))
    val encoded = truncated.toString(36)
    return encoded.takeLast(8).padStart(8, '0')
}

/**
 * Builds the canonical key `<sourceFqn>(<paramTypeFqn1>,<paramTypeFqn2>,...)`.
 * Callers must align how `parameterTypeFqns` is produced on the FIR and IR sides so that
 * the same `@Preview` always yields the same key.
 *
 * **Sample**: `buildPreviewHintCanonicalKey("uiLib.MyButton", emptyList())` → `"uiLib.MyButton()"`
 * **Sample**: `buildPreviewHintCanonicalKey("uiLib.MyButton", listOf("kotlin.String"))` → `"uiLib.MyButton(kotlin.String)"`
 */
internal fun buildPreviewHintCanonicalKey(sourceFqn: String, parameterTypeFqns: List<String>): String =
    "$sourceFqn(${parameterTypeFqns.joinToString(",")})"

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
    return "${PreviewLabConstants.PreviewHintMarkerPrefix}${sanitizedFqn}_$hash"
}

/** Characters that are not legal in a non-backticked Kotlin identifier. */
private val NonIdentifierCharRegex = Regex("[^A-Za-z0-9_]")
