package me.tbsten.compose.preview.lab.compiler

/**
 * Numeric `MAJOR.MINOR.PATCH` comparison helper. Avoids the bug where lexicographic
 * comparison would judge `"2.3.9"` as `>= 2.3.21`.
 *
 * **Samples**:
 * - `"2.3.21".isAtLeast(2, 3, 21)` → `true`
 * - `"2.3.9".isAtLeast(2, 3, 21)` → `false`
 * - `"2.4.0".isAtLeast(2, 3, 21)` → `true`
 * - `"2.3.21-Beta1".isAtLeast(2, 3, 21)` → `true` (the suffix is ignored)
 */
internal fun String.isAtLeast(major: Int, minor: Int, patch: Int): Boolean {
    val parts = substringBefore('-').split('.')
    val v0 = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val v1 = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val v2 = parts.getOrNull(2)?.toIntOrNull() ?: 0
    return when {
        v0 != major -> v0 > major
        v1 != minor -> v1 > minor
        else -> v2 >= patch
    }
}
