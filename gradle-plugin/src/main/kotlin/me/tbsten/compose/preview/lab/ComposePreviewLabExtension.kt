package me.tbsten.compose.preview.lab

import javax.inject.Inject
import me.tbsten.compose.preview.lab.util.invoke
import me.tbsten.compose.preview.lab.util.ksp
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

abstract class ComposePreviewLabExtension @Inject constructor(objects: ObjectFactory, project: Project) {
    val generatePackage: Property<String> = objects.property<String>()
        .convention(
            project.name
                .split(Regex("(\\.)|_|-"))
                .mapIndexed { index, word -> if (index == 0) word else word.replaceFirstChar { it.uppercase() } }
                .joinToString(""),
        )

    val publicPreviewList: Property<Boolean> = objects.property<Boolean>()
        .convention(false)

    val projectRootPath: Property<String?> = objects.property<String?>()
        .convention(project.rootProject.projectDir.absolutePath)
}

internal fun Project.applyToKspExtension(extension: ComposePreviewLabExtension) {
    afterEvaluate {
        ksp {
            arg("composePreviewLab.previewsListPackage", extension.generatePackage.orNull ?: "")
            arg("composePreviewLab.publicPreviewList", extension.publicPreviewList.get().toString())
            arg("composePreviewLab.projectRootPath", extension.projectRootPath.orNull ?: "")
        }
    }
}
