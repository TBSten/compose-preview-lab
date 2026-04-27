package me.tbsten.compose.preview.lab.compiler.compat

import java.util.ServiceLoader

/**
 * [CompatContext.Factory] を ServiceLoader 経由で発見し、
 * 現在の Kotlin compiler バージョンに最適な実装を返す。
 */
internal object CompatContextLoader {

    /**
     * 現在の Kotlin バージョンに対して `currentVersion >= minVersion` を満たす
     * factory のうち、`minVersion` が最大のものを選ぶ。
     *
     * 例: factories = [k230 (2.3.0), k240_beta2 (2.4.0-Beta2)]
     *   - currentVersion = 2.3.21 → k230
     *   - currentVersion = 2.4.0-Beta2 → k240_beta2
     *   - currentVersion = 2.4.0 (stable) → k240_beta2 (Beta < STABLE なので互換)
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
                    "compiler-plugin-compat-* modules may not be bundled into the plugin jar.",
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
     * `META-INF/compiler.version` ファイルから Kotlin compiler バージョンを取得する。
     * このファイルは `kotlin-compiler-embeddable` jar に同梱されている。
     */
    private fun detectCurrentKotlinVersion(): KotlinToolingVersion? {
        val classLoader = CompatContextLoader::class.java.classLoader ?: return null
        val resource = classLoader.getResourceAsStream("META-INF/compiler.version") ?: return null
        val text = resource.use { it.bufferedReader().readText().trim() }
        if (text.isEmpty()) return null
        return runCatching { KotlinToolingVersion(text) }.getOrNull()
    }
}
