package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import java.security.MessageDigest

// Canonical-key + hash computation used to derive both the per-`@Preview` marker class
// short name and the hint function short name.
//
// The canonical key is a stable, environment-independent string formed from the source
// FQN plus the parameter-type FQN list. Two `@Preview` declarations with the same FQN
// but different parameter types (overloads) produce distinct canonical keys, so they
// receive distinct hashes and never collide on the marker class name.
//
// The hint suffix and marker short name themselves live in:
// - HintFunName.kt — `previewHint_<scope>` (no hash; scope-based)
// - MarkerInterfaceName.kt — `PreviewHintMarker_<sanitized_fqn>_<hash>`

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
 * **Result**: an 8-char base-36 string such as `"a3k9z2x1"`.
 *
 * SHA-256 → first 8 bytes (64 bits) → base-36 encoding → last 8 characters. The
 * resulting alphabet has ~41 effective bits, which keeps the collision probability
 * negligibly small for practical namespace sizes.
 */
internal fun computeHintHash(canonicalKey: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(canonicalKey.toByteArray(Charsets.UTF_8))
    val truncated = java.math.BigInteger(1, digest.copyOf(8))
    val encoded = truncated.toString(36)
    return encoded.takeLast(8).padStart(8, '0')
}

/**
 * Builds the canonical key `<sourceFqn>(<paramTypeFqn1>,<paramTypeFqn2>,...)` that
 * [computeHintHash] hashes.
 *
 * Callers must align how `parameterTypeFqns` is produced on the FIR and IR sides so the
 * same `@Preview` always yields the same key — see [ParameterTypeFqns] (single source of
 * truth for the format spec, shared by FIR and IR extension functions).
 *
 * **Sample**: `buildPreviewHintCanonicalKey("uiLib.MyButton", emptyList())` → `"uiLib.MyButton()"`
 *
 * **Sample**: `buildPreviewHintCanonicalKey("uiLib.MyButton", listOf("kotlin.String"))` → `"uiLib.MyButton(kotlin.String)"`
 */
internal fun buildPreviewHintCanonicalKey(sourceFqn: String, parameterTypeFqns: List<String>): String =
    "$sourceFqn(${parameterTypeFqns.joinToString(",")})"
