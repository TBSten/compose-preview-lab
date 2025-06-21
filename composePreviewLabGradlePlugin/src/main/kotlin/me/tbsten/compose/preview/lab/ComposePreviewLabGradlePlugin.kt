package me.tbsten.compose.preview.lab

import me.tbsten.compose.preview.lab.util.invoke
import me.tbsten.compose.preview.lab.util.ksp
import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposePreviewLabGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        setDefaultValues()
    }
}

internal fun Project.setDefaultValues() {
    ksp {
        arg(
            "composePreviewLab.previewsListPackage",
            project.name
                .split(Regex("(\\.)|_|-"))
                .mapIndexed { index, word -> if (index == 0) word else word.replaceFirstChar { it.uppercase() } }
                .joinToString(""),
        )
        println("configure composePreviewLab.previewsListPackage:")
        println(
            "  ${
                project.name
                    .split(Regex(".|_|-"))
            }",
        )
        println("  - project.name: ${project.name}")
        println(
            "  - computed value: ${
                project.name
                    .split(Regex(".|_|-"))
                    .mapIndexed { index, word -> if (index == 0) word else word.replaceFirstChar { it.uppercase() } }
                    .joinToString("")
            }",
        )
        println("  - ksp.arguments: ${arguments["composePreviewLab.previewsListPackage"]}")
        arg(
            "composePreviewLab.projectRootPath",
            project.rootProject.projectDir.absolutePath,
        )
    }
}
