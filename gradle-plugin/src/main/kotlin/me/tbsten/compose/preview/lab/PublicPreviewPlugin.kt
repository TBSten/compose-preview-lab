package me.tbsten.compose.preview.lab

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class PublicPreviewPlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        println("PublicPreviewPlugin.apply(${target.name})")
        target.extensions.create(
            "publicPreview",
            PublicPreviewExtension::class.java,
        )
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        println("PublicPreviewPlugin.applyToCompilation: $kotlinCompilation")

        val extension =
            kotlinCompilation.project.extensions.findByType(PublicPreviewExtension::class.java)
                ?: PublicPreviewExtension()

        if (extension.enabled && extension.annotations.isEmpty()) {
            error("MyPlugin is enabled but no annotations are specified.")
        }

        val enabledOption =
            SubpluginOption(
                key = "enabled",
                value = extension.enabled.toString(),
            )
        val annotationOptions =
            extension.annotations.map {
                SubpluginOption(key = "publicPreviewAnnotation", value = it)
            }
        println(
            (listOf(enabledOption) + annotationOptions).joinToString("\n") {
                "  - ${it.key}: ${it.value}"
            },
        )
        return kotlinCompilation.target.project.provider {
            listOf(enabledOption) + annotationOptions
        }
    }

    override fun getCompilerPluginId(): String = "cream-public-preview-plugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
//        groupId = "me.tbsten.compose.preview.lab",
//        artifactId = "compiler-plugin",
//        version = "0.1.0-dev06",
        groupId = "test",
        artifactId = "compiler-plugin",
        version = "0.1.0-dev06",
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
    //        kotlinCompilation.project.plugins.hasPlugin(PublicPreviewPlugin::class.java)
}
