package me.tbsten.compose.preview.lab.compiler.compat

/**
 * Kotlin tooling version with maturity-aware comparison.
 *
 * Adapted from Metro's `KotlinToolingVersion`
 * (https://github.com/ZacSweers/metro, Apache 2.0). Trimmed to the subset
 * required by [CompatContext.Factory] selection.
 *
 * Examples:
 * - "2.3.0", "2.3.21", "2.4.0-Beta2"
 * - "2.4.0-dev-2124"
 * - "2.3.0-RC", "2.3.0-RC2"
 */
public class KotlinToolingVersion(
    public val major: Int,
    public val minor: Int,
    public val patch: Int,
    public val classifier: String?,
) : Comparable<KotlinToolingVersion> {

    public val maturity: Maturity = run {
        val c = classifier?.lowercase()
        when {
            c == null || c.matches(Regex("""(release-)?\d+""")) -> Maturity.STABLE
            c == "snapshot" -> Maturity.SNAPSHOT
            c.matches(Regex("""rc\d*(-release)?(-?\d+)?""")) -> Maturity.RC
            c.matches(Regex("""beta\d*(-release)?(-?\d+)?""")) -> Maturity.BETA
            c.matches(Regex("""alpha\d*(-release)?(-?\d+)?""")) -> Maturity.ALPHA
            c.matches(Regex("""m\d+(-release)?(-\d+)?""")) -> Maturity.MILESTONE
            else -> Maturity.DEV
        }
    }

    public val isDev: Boolean get() = maturity == Maturity.DEV

    private val classifierNumber: Int? by lazy {
        classifier?.let { Regex("""(?:rc|beta|alpha|m)(\d+)""", RegexOption.IGNORE_CASE).find(it) }
            ?.groupValues?.get(1)?.toIntOrNull()
    }

    private val buildNumber: Int? by lazy {
        // Trailing number such as in "dev-2124".
        classifier?.let { Regex("""-(\d+)$""").find(it) }?.groupValues?.get(1)?.toIntOrNull()
    }

    override fun compareTo(other: KotlinToolingVersion): Int {
        (this.major - other.major).takeIf { it != 0 }?.let { return it }
        (this.minor - other.minor).takeIf { it != 0 }?.let { return it }
        (this.patch - other.patch).takeIf { it != 0 }?.let { return it }
        (this.maturity.ordinal - other.maturity.ordinal).takeIf { it != 0 }?.let { return it }
        // For STABLE: a missing classifier ranks higher than "release-N".
        if (this.classifier == null && other.classifier != null) return 1
        if (this.classifier != null && other.classifier == null) return -1
        val a = classifierNumber ?: 0
        val b = other.classifierNumber ?: 0
        (a - b).takeIf { it != 0 }?.let { return it }
        val ab = buildNumber ?: 0
        val bb = other.buildNumber ?: 0
        (ab - bb).takeIf { it != 0 }?.let { return it }
        return 0
    }

    override fun equals(other: Any?): Boolean = other is KotlinToolingVersion && compareTo(other) == 0

    override fun hashCode(): Int {
        var r = major
        r = 31 * r + minor
        r = 31 * r + patch
        r = 31 * r + (classifier?.lowercase()?.hashCode() ?: 0)
        return r
    }

    override fun toString(): String = if (classifier != null) "$major.$minor.$patch-$classifier" else "$major.$minor.$patch"

    public enum class Maturity { SNAPSHOT, DEV, MILESTONE, ALPHA, BETA, RC, STABLE }
}

/** Parses a string of the form "2.3.0", "2.4.0-Beta2", or "2.4.0-dev-2124". */
public fun KotlinToolingVersion(versionString: String): KotlinToolingVersion {
    val base = versionString.substringBefore('-')
    val classifier = if ('-' in versionString) versionString.substringAfter('-') else null
    val parts = base.split('.')
    val isValid = parts.size == 3 && parts.all { it.toIntOrNull() != null }
    if (!isValid) {
        System.err.println(
            "WARNING: [ComposePreviewLab] Unrecognised Kotlin version string '$versionString'; " +
                "falling back to 0.0.0. Factory selection may be incorrect.",
        )
    }
    return KotlinToolingVersion(
        major = parts.getOrNull(0)?.toIntOrNull() ?: 0,
        minor = parts.getOrNull(1)?.toIntOrNull() ?: 0,
        patch = parts.getOrNull(2)?.toIntOrNull() ?: 0,
        classifier = classifier,
    )
}
