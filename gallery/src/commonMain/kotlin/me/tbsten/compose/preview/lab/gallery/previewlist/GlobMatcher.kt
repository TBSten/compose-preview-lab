package me.tbsten.compose.preview.lab.gallery.previewlist

/**
 * Check if a pattern is a glob pattern.
 *
 * A glob pattern contains at least one of:
 * - `*` (matches any characters except `/`)
 * - `**` (matches any path including `/`)
 * - `?` (matches any single character)
 */
internal fun isGlobPattern(pattern: String): Boolean = pattern.contains('*') || pattern.contains('?')

/**
 * Convert a glob pattern to a regular expression.
 *
 * Supported glob syntax:
 * - `*` matches any characters except `/`
 * - `**` matches any characters including `/`
 * - `?` matches any single character
 * - Other characters are escaped for regex
 */
internal fun globToRegex(glob: String): Regex {
    val regexPattern = buildString {
        append("^")
        var i = 0
        while (i < glob.length) {
            when {
                glob.startsWith("**/", i) -> {
                    // **/ at start or after / matches zero or more directories
                    append("(?:.*/)?")
                    i += 3
                }
                glob.startsWith("**", i) -> {
                    // ** matches any path (including /)
                    append(".*")
                    i += 2
                }
                glob[i] == '*' -> {
                    // * matches any characters except /
                    append("[^/]*")
                    i++
                }
                glob[i] == '?' -> {
                    // ? matches any single character
                    append(".")
                    i++
                }
                else -> {
                    // Escape regex special characters
                    append(Regex.escape(glob[i].toString()))
                    i++
                }
            }
        }
        append("$")
    }
    return Regex(regexPattern)
}

/**
 * Check if the given path matches a glob pattern.
 *
 * @param path The file path to check
 * @param pattern The glob pattern or exact path
 * @return true if the path matches the pattern
 */
internal fun matchesGlob(path: String, pattern: String): Boolean {
    // Normalize path separators
    val normalizedPath = path.replace('\\', '/')
    val normalizedPattern = pattern.replace('\\', '/')

    return if (isGlobPattern(normalizedPattern)) {
        val regex = globToRegex(normalizedPattern)
        regex.matches(normalizedPath)
    } else {
        // Exact match
        normalizedPath == normalizedPattern
    }
}
