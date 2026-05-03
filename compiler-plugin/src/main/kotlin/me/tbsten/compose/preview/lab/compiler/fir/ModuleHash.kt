package me.tbsten.compose.preview.lab.compiler.fir

import java.security.MessageDigest

/**
 * Computes a stable, collision-resistant 8-character base-36 hash from a Kotlin module name.
 *
 * Used by both [PreviewLabHintEntries] (FIR side, marker class suffix) and the IR-side
 * `computeAutoProviderName` (provider function suffix) so they produce identical suffixes for
 * the same module — that identity is what lets downstream
 * `PreviewListIrBuilder.collectDependencyGetters()` reconstruct the provider FQN from the
 * marker class id alone, with no `@PreviewExportHint` annotation lookup.
 *
 * **Why not `String.hashCode()`**: Java's `String.hashCode()` is 32-bit and has well-known
 * collision pairs (e.g. `"Aa".hashCode() == "BB".hashCode()`). Module names are user-controlled,
 * so a 32-bit hash invites real-world collisions that recreate the very KLIB IdSignature clash
 * this hint scheme is meant to eliminate. SHA-256 (truncated to 8 base-36 chars ≈ 41 bits) gives
 * collision odds of ~1 in 2.2 trillion across the module namespace, which is comfortably below
 * the practical threshold even for monorepos.
 *
 * **Sample**: `computeModuleHash(":uiLib")` → `"a3k9z2x1"` (stable across runs, JVM versions).
 */
internal fun computeModuleHash(moduleName: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(moduleName.toByteArray(Charsets.UTF_8))
    // Take the first 8 bytes (64 bits) and turn them into a positive BigInteger, then base-36
    // encode and pad/truncate to exactly 8 chars. `Long.toString(36)` would also work but
    // BigInteger keeps the code path identical when we want to extend the hash length later.
    val truncated = java.math.BigInteger(1, digest.copyOf(8))
    val encoded = truncated.toString(36)
    return encoded.takeLast(8).padStart(8, '0')
}
