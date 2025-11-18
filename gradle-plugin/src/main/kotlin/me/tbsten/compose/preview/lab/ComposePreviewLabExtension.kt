package me.tbsten.compose.preview.lab

import javax.inject.Inject
import me.tbsten.compose.preview.lab.internal.KspArg
import me.tbsten.compose.preview.lab.util.invoke
import me.tbsten.compose.preview.lab.util.ksp
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.setValue

abstract class ComposePreviewLabExtension @Inject constructor(objects: ObjectFactory, project: Project) {
    var generatePackage: String by objects.property<String>()
        .convention(
            project.name
                .split(Regex("(\\.)|_|-"))
                .mapIndexed { index, word -> if (index == 0) word else word.replaceFirstChar { it.uppercase() } }
                .joinToString(""),
        )

    var publicPreviewList: Boolean by objects.property<Boolean>()
        .convention(false)

    var projectRootPath: String by objects.property<String>()
        .convention(project.rootProject.projectDir.absolutePath)

    var generatePreviewList: Boolean by objects.property<Boolean>()
        .convention(true)

    var generatePreviewAllList: Boolean by objects.property<Boolean>()
        .convention(true)

    var generateFeaturedFiles: Boolean by objects.property<Boolean>()
        .convention(false)
}

internal fun Project.applyToKspExtension(extension: ComposePreviewLabExtension) {
    afterEvaluate {
        ksp {
            arg(KspArg.previewsListPackage, extension.generatePackage)
            arg(KspArg.publicPreviewList, extension.publicPreviewList.toString())
            arg(KspArg.projectRootPath, extension.projectRootPath)
            arg(KspArg.generatePreviewList, extension.generatePreviewList.toString())
            arg(KspArg.generatePreviewAllList, extension.generatePreviewAllList.toString())
        }
    }
}
