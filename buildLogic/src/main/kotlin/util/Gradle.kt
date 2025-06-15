package util

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.PluginManager
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.getByType
import org.gradle.plugin.use.PluginDependency

// version catalog

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.library(name: String): MinimalExternalModuleDependency {
    return findLibrary(name).get().get()
}

internal fun VersionCatalog.plugin(name: String): PluginDependency {
    return findPlugin(name).get().get()
}

internal fun VersionCatalog.version(name: String): String {
    return findVersion(name).get().requiredVersion
}

// plugins

fun Project.plugins(block: PluginManager.() -> Unit) {
    pluginManager.apply(block)
}

internal fun PluginManager.alias(plugin: PluginDependency) {
    apply(plugin.pluginId)
}

// dependencies

internal fun DependencyHandlerScope.implementation(
    artifact: MinimalExternalModuleDependency,
) {
    add("implementation", artifact)
}

internal fun DependencyHandlerScope.implementation(
    project: Project,
) {
    add("implementation", project)
}

internal fun DependencyHandlerScope.implementationPlatform(
    artifact: MinimalExternalModuleDependency,
) {
    add("implementation", platform(artifact))
}
