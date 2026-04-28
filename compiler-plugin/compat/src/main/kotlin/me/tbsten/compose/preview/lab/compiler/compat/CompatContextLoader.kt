package me.tbsten.compose.preview.lab.compiler.compat

import java.util.ServiceLoader

/**
 * Discovers [CompatContext.Factory] implementations via ServiceLoader and
 * returns the one that best matches the current Kotlin compiler version.
 */
internal object CompatContextLoader {

    /**
     * Among the factories whose `minVersion` is `<= currentVersion`, picks the one
     * with the largest `minVersion`.
     */
    fun load(knownVersion: KotlinToolingVersion? = null): CompatContext {
        val factories = ServiceLoader
            .load(
                CompatContext.Factory::class.java,
                CompatContext.Factory::class.java.classLoader,
            )
            .toList()

        if (factories.isEmpty()) {
            error(
                "No CompatContext.Factory implementations found on the classpath. " +
                    "The :compiler-plugin:compat-k* modules may not be bundled into the published plugin jar.",
            )
        }

        val currentVersion = knownVersion
            ?: detectCurrentKotlinVersion()
            ?: error(
                "Could not detect Kotlin compiler version (META-INF/compiler.version not found). " +
                    "Available factories: ${factories.map { it.minVersion }}",
            )

        val compatible = factories.filter { factory ->
            runCatching { currentVersion >= KotlinToolingVersion(factory.minVersion) }
                .getOrDefault(false)
        }

        val best = compatible.maxByOrNull { KotlinToolingVersion(it.minVersion) }
            ?: error(
                "No compatible CompatContext.Factory for Kotlin $currentVersion. " +
                    "Available: ${factories.map { it.minVersion }}",
            )

        return best.create()
    }

    /**
     * Reads the Kotlin compiler version from `META-INF/compiler.version`.
     * That file is shipped inside the `kotlin-compiler-embeddable` jar.
     */
    private fun detectCurrentKotlinVersion(): KotlinToolingVersion? {
        val classLoader = CompatContextLoader::class.java.classLoader ?: return null
        val resource = classLoader.getResourceAsStream("META-INF/compiler.version") ?: return null
        val text = resource.use { it.bufferedReader().readText().trim() }
        if (text.isEmpty()) return null
        return runCatching { KotlinToolingVersion(text) }.getOrNull()
    }
}
