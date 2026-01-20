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

/**
 * Configuration extension for Compose Preview Lab Gradle plugin
 *
 * Provides configuration options for customizing the code generation and preview collection
 * behavior of Compose Preview Lab. Applied in build.gradle.kts using the `composePreviewLab` block.
 *
 * ```kotlin
 * composePreviewLab {
 *     generatePackage = "myModule"
 *     publicPreviewList = true
 *     generateFeaturedFiles = true
 * }
 * ```
 */
public abstract class ComposePreviewLabExtension @Inject constructor(objects: ObjectFactory, project: Project) {
    /**
     * Package name for generated preview lists
     *
     * Specifies the package where PreviewList and PreviewAllList objects will be generated.
     * Defaults to a camelCase version of the project name.
     *
     * Example: For project "my-app", defaults to "myApp"
     */
    public var generatePackage: String by objects.property<String>()
        .convention(
            project.name
                .split(Regex("(\\.)|_|-"))
                .mapIndexed { index, word -> if (index == 0) word else word.replaceFirstChar { it.uppercase() } }
                .joinToString(""),
        )

    /**
     * Controls visibility of generated preview lists
     *
     * When true, generates public PreviewList objects accessible from other modules.
     * When false (default), generates internal objects for module-local use only.
     */
    public var publicPreviewList: Boolean by objects.property<Boolean>()
        .convention(false)

    /**
     * Root path of the project for file path resolution
     *
     * Used to resolve relative file paths in generated previews.
     * Defaults to the root project directory.
     */
    public var projectRootPath: String by objects.property<String>()
        .convention(project.rootProject.projectDir.absolutePath)

    /**
     * Controls generation of PreviewList object
     *
     * When true (default), generates a PreviewList object containing all previews
     * from the current module.
     */
    public var generatePreviewList: Boolean by objects.property<Boolean>()
        .convention(true)

    /**
     * Controls generation of PreviewAllList aggregated object
     *
     * When true (default), generates a PreviewAllList object that aggregates
     * previews from the current module and all dependencies marked with @AggregateToAll.
     */
    public var generatePreviewAllList: Boolean by objects.property<Boolean>()
        .convention(true)

    /**
     * Controls generation of FeaturedFileList from .composepreviewlab/featured/ directory
     *
     * When true, scans the .composepreviewlab/featured/ directory and generates
     * a FeaturedFileList map grouping file paths by directory name.
     * When false (default), no FeaturedFileList is generated.
     */
    public var generateFeaturedFiles: Boolean by objects.property<Boolean>()
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
