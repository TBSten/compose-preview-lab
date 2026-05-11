package me.tbsten.compose.preview.lab.compiler.feature.previewCollection.ir.collectPreviewsReplacement

/**
 * Hint hash collision-detecting `(canonical key, value)` → `hash → value` map builder.
 *
 * **This is intentionally specialized to preview hint hashing — do not generalize without
 * moving to `utils/`.** The data shape (canonical key → hash → value with collision
 * callback) is generic on the surface, but the lifecycle and the call-site contract
 * are scoped to [BuildPreviewByHashMap] / [FillPreviewHintIrBody]. Lifting this to
 * `utils/` would require severing the preview-specific callback shape (e.g. the
 * "same canonical key = idempotent overwrite" rule reflects how the FIR generator
 * de-duplicates `@Preview` symbols). When that level of generality is truly needed,
 * extract a non-callback `Map<String, V>` variant in `utils/` and wrap the preview-specific
 * callback in a thin builder kept in this logic directory.
 *
 * **Sample (collision case)**:
 * ```kotlin
 * val hits = mutableListOf<String>()
 * val map = buildHashMapWithCollisionDetection(
 *     entries = listOf("keyA" to "A", "keyB" to "B"),
 *     hash = { "h" }, // force collision
 *     onCollision = { _, existing, conflicting -> hits += "$existing/$conflicting" },
 * )
 * // map = { "h" -> "B" }, hits = ["A/B"]
 * ```
 *
 * **Sample (idempotent overwrite, no collision)**:
 * ```kotlin
 * buildHashMapWithCollisionDetection(
 *     entries = listOf("keyA" to "A", "keyA" to "A2"),
 *     hash = { it },
 *     onCollision = { _, _, _ -> error("should not fire") },
 * )
 * // map = { "keyA" -> "A2" } — re-registering the same key silently overwrites
 * ```
 */
internal fun <V> buildHashMapWithCollisionDetection(
    entries: List<Pair<String, V>>,
    hash: (canonicalKey: String) -> String,
    onCollision: (hash: String, existing: V, conflicting: V) -> Unit = { _, _, _ -> },
): Map<String, V> = buildMap {
    val canonicalKeyByHash = mutableMapOf<String, String>()
    for ((canonicalKey, value) in entries) {
        val h = hash(canonicalKey)
        val existingKey = canonicalKeyByHash[h]
        if (existingKey != null && existingKey != canonicalKey) {
            // Two distinct canonical keys landed on the same hash = a genuine collision.
            // Re-registering the same canonical key (e.g. a file walked twice across the
            // ignore=true filter passes) is just noise, so silently overwrite.
            val existing = getValue(h)
            onCollision(h, existing, value)
        }
        canonicalKeyByHash[h] = canonicalKey
        put(h, value)
    }
}
