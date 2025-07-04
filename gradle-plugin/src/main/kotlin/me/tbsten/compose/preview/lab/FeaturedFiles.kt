package me.tbsten.compose.preview.lab

import com.google.devtools.ksp.gradle.KspTaskMetadata
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureFeaturedFiles(extension: ComposePreviewLabExtension) {
    val outputDir = layout.buildDirectory.dir("generated/composepreviewlab/")
    val internalGenerateFeaturedFilesCode = tasks.register<GenerateFeaturedFilesCode>("internalGeneratefeaturedFilesCode") {
        group = "compose preview lab internal"
        this.packageName = extension.previewsListPackage.get()
        this.featuredFilesDir =
            rootProject
                .layout.projectDirectory
                .dir(".composepreviewlab/featured")
        this.projectRootPath = extension.projectRootPath.get()
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
            // KSP と共存するために設定を加える必要がある
            // refs: https://github.com/google/ksp/issues/963#issuecomment-1894144639
            kotlin.srcDir(
                tasks.withType<KspTaskMetadata>().map { it.destinationDirectory },
            )
        }
    }
}

internal abstract class GenerateFeaturedFilesCode : DefaultTask() {
    @get:Input
    abstract var packageName: String

    @get:InputDirectory
    abstract var featuredFilesDir: Directory

    @get:Input
    abstract var projectRootPath: String

    @get:OutputDirectory
    abstract var outputDir: Provider<Directory>

    @TaskAction
    fun generateCode() {
        var featuredFilesCode = """
            package $packageName

            data object FeaturedFiles : Map<String, List<String>> by mapOf(
        """.trimIndent()
        featuredFilesCode += "\n"

        val groupNames = mutableListOf<String>()
        featuredFilesDir.asFile.listFiles().forEach { featuredFileFile ->
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
