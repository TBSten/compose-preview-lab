package me.tbsten.compose.preview.lab.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

/**
 *
 * プロジェクト内から @Preview を収集し modulePackage.previews で 収集した Preview にアクセスできるようにする。
 * 収集した Preview は PreviewLabRoot に渡すことで Compose Preview Lab の UI として表示させることができる。
 *
 * refs: https://tech.mirrativ.stream/entry/2025/03/27/100000
 */
class ComposePreviewLabPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        extensions.create("composePreviewLab", ComposePreviewLabExtension::class.java)

        val kotlinMultiplatformExtension = extensions.findByType<KotlinMultiplatformExtension>()
            ?: error("Kotlin Multiplatform plugin must be applied before Compose Preview Lab plugin")

        afterEvaluate {
            val generateDir = layout.buildDirectory.dir("generated/compose-preview-lab/commonMain")
            kotlinMultiplatformExtension.sourceSets.named("commonMain").get()
                .kotlin.srcDir(generateDir)

            val kotlinVersion = getKotlinPluginVersion()

            val myDependencyScope = configurations.create("myDependencyScope")
            dependencies.add(
                myDependencyScope.name,
                "org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion"
            )
            val myResolvableConfiguration = configurations.create("myResolvable") {
                extendsFrom(myDependencyScope)
            }

            val prepareGeneratePreviewSources = tasks.register("prepareGeneratePreviewSources") {
                doLast { generateDir.get().asFile.mkdirs() }
            }
            tasks.register("generatePreviewSources", TaskUsingKotlinCompiler::class.java) {
                dependsOn(prepareGeneratePreviewSources)
                kotlinCompiler.from(myResolvableConfiguration)
                srcDirs.set(
                    kotlinMultiplatformExtension.sourceSets.named("commonMain").get().kotlin.srcDirs
                        .filter { it != generateDir.get().asFile }
                )
                modulePackage.set(project.name.replace(Regex("[-./:]"), "_"))
                generateDestinationDir.set(generateDir)
            }
            tasks.findByName("clean")?.doLast {
                generateDir.get().asFile.deleteRecursively()
            }
        }
    }
}
