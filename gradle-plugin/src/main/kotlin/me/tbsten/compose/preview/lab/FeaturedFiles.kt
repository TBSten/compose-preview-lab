package me.tbsten.compose.preview.lab

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureFeaturedFiles(extension: ComposePreviewLabExtension) {
    afterEvaluate {
        if (extension.generateFeaturedFiles) {
            val outputDir = layout.buildDirectory.dir("generated/composepreviewlab/")
            val internalGenerateFeaturedFilesCode =
                tasks.register<GenerateFeaturedFilesCode>("internalGeneratefeaturedFilesCode") {
                    group = "compose preview lab internal"
                    this.packageName = extension.generatePackage
                    this.featuredFilesDir.set(
                        rootProject
                            .layout.projectDirectory
                            .dir(".composepreviewlab/featured"),
                    )
                    this.projectRootPath = extension.projectRootPath
                    this.outputDir = outputDir.also { it.get().asFile.mkdirs() }
                }

            tasks.withType<KotlinCompile> {
                dependsOn(internalGenerateFeaturedFilesCode)
                mustRunAfter(internalGenerateFeaturedFilesCode)
            }

            // kotlin.sourceSets に出力先を追加
            listOf(
                "main",
                "commonMain",
            ).forEach { sourceSetName ->
                kotlinExtension.sourceSets.findByName(sourceSetName)?.apply {
                    kotlin.srcDir(internalGenerateFeaturedFilesCode)
                }
            }
        }
    }
}

internal abstract class GenerateFeaturedFilesCode : DefaultTask() {
    @get:Input
    abstract var packageName: String

    @get:Internal
    abstract val featuredFilesDir: DirectoryProperty

    @get:Input
    abstract var projectRootPath: String

    @get:OutputDirectory
    abstract var outputDir: Provider<Directory>

    @TaskAction
    fun generateCode() {
        var featuredFilesCode = """
            package $packageName

            data object FeaturedFileList : Map<String, List<String>> by mapOf(
        """.trimIndent()
        featuredFilesCode += "\n"

        val groupNames = mutableListOf<String>()

        featuredFilesDir.asFile.orNull?.let { featuredFilesDir ->
            if (featuredFilesDir.exists()) {
                featuredFilesDir.listFiles()?.sorted()?.forEach { featuredFileFile ->
                    val groupName = featuredFileFile.name
                        .also { groupNames.add(it) }
                    featuredFileFile.useLines { lines ->
                        val featuredFiles = lines
                            .filter { line -> line.isNotBlank() }
                            .toList()

                        if (featuredFiles.isNotEmpty()) {
                            featuredFilesCode += """
                    |    // ${featuredFileFile.path}
                    |    "$groupName" to listOf(
                    |${featuredFiles.joinToString(",\n") { "        \"$it\"" }}
                    |    ),
                            """.trimMargin() + "\n"
                        }
                    }
                }
            }
        }

        featuredFilesCode += ") {\n"
        groupNames.forEach { group ->
            featuredFilesCode += "    val `$group` get() = this[\"$group\"]!!\n"
        }
        featuredFilesCode += "}"

        outputDir.get()
            .dir(packageName.replace(".", "/"))
            .file("featuredFiles.kt")
            .also { it.asFile.parentFile.mkdirs() }
            .asFile
            .bufferedWriter()
            .use { it.write(featuredFilesCode) }
    }
}
