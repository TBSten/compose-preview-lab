package me.tbsten.compose.preview.lab

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

internal const val PreviewLabCompilerPluginId = "me.tbsten.compose.preview.lab.compiler"
internal const val ComposeCompilerPluginId = "androidx.compose.compiler.plugins.kotlin"

internal data class KotlinSemVer(val major: Int, val minor: Int, val patch: Int) : Comparable<KotlinSemVer> {
    override fun compareTo(other: KotlinSemVer) = compareValuesBy(
        this,
        other,
        KotlinSemVer::major,
        KotlinSemVer::minor,
        KotlinSemVer::patch,
    )

    companion object {
        fun parse(raw: String): KotlinSemVer? {
            val coreVersion = raw.substringBefore('-').substringBefore('+')
            val parts = coreVersion.split('.')
            if (parts.size < 2) return null

            val major = parts[0].toIntOrNull() ?: return null
            val minor = parts[1].toIntOrNull() ?: return null
            val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0

            return KotlinSemVer(major, minor, patch)
        }
    }
}

private val kotlin230 = KotlinSemVer(2, 3, 0)

internal fun Project.detectedKotlinVersion(): KotlinSemVer? =
    runCatching { getKotlinPluginVersion() }.getOrNull()?.let(KotlinSemVer::parse)

internal fun Project.supportsCompilerPluginOrder(): Boolean {
    val version = detectedKotlinVersion() ?: return false
    return version >= kotlin230
}
