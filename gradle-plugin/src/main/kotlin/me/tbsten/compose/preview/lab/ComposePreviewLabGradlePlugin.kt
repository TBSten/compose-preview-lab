package me.tbsten.compose.preview.lab

import com.google.devtools.ksp.gradle.KspExtension
import me.tbsten.compose.preview.lab.util.invoke
import me.tbsten.compose.preview.lab.util.ksp
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * A Gradle plugin for configuring the Compose Preview Lab.
 * It sets default values for KSP arguments used by the library.
 * It also provides a typesafe helper (`ksp.composePreviewLab.***`) for KSP configuration.
 */
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
        arg(
            "composePreviewLab.projectRootPath",
            project.rootProject.projectDir.absolutePath,
        )
    }
}

/**
 * Provides a typesafe accessor for KSP arguments related to Compose Preview Lab.
 * Use this to configure the library in your build script.
 *
 * ```kt
 * ksp {
 *   composePreviewLab {
 *     previewsListPackage = "myModule"
 *     publicPreviewList = true
 *   }
 * }
 * ```
 *
 * @see TypeSafeComposePreviewLabKspArgAccessor
 */
val KspExtension.composePreviewLab get() = TypeSafeComposePreviewLabKspArgAccessor(this)

/**
 * Provides a typesafe accessor for KSP arguments related to Compose Preview Lab.
 * Use this to configure the library in your build script.
 *
 * @property previewsListPackage Get/Set `composePreviewLab.previewsListPackage`.
 * @property publicPreviewList Get/Set `composePreviewLab.publicPreviewList`.
 * @property projectRootPath Get/Set the `composePreviewLab.projectRootPath`. In most cases, you can just use the default value set by the Gradle Plugin.
 */
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
