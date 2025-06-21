package me.tbsten.compose.preview.lab

import com.google.devtools.ksp.gradle.KspExtension
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

val KspExtension.composePreviewLab get() = TypeSafeComposePreviewLabKspArgAccessor(this)

class TypeSafeComposePreviewLabKspArgAccessor internal constructor(private val ksp: KspExtension) {
    var previewsListPackage: String?
        get() = ksp.arguments["composePreviewLab.previewsListPackage"]
        set(value) {
            if (value != null) ksp.arg("composePreviewLab.previewsListPackage", value)
        }

    var publicPreviewList: Boolean
        get() = ksp.arguments["composePreviewLab.publicPreviewList"]?.lowercase() == "true"
        set(value) = ksp.arg("composePreviewLab.publicPreviewList", value.toString())

    var projectRootPath: String?
        get() = ksp.arguments["composePreviewLab.projectRootPath"]
        set(value) = ksp.arg("composePreviewLab.projectRootPath", value ?: "")
}
