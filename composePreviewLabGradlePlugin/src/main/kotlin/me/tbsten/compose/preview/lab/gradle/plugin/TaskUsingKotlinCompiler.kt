package me.tbsten.compose.preview.lab.gradle.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

abstract class TaskUsingKotlinCompiler : DefaultTask() {
    @get:Inject
    abstract val executor: WorkerExecutor

    @get:Classpath
    abstract val kotlinCompiler: ConfigurableFileCollection

    @get:Input
    abstract val srcDirs: SetProperty<File>

    @get:OutputDirectory
    abstract val generateDestinationDir: DirectoryProperty

    @get:Input
    abstract val modulePackage: Property<String>

    @TaskAction
    fun compile() {
        val workQueue = executor.classLoaderIsolation {
            classpath.from(kotlinCompiler)
        }
        workQueue.submit(ActionUsingKotlinCompiler::class.java) {
            srcDirs.set(this@TaskUsingKotlinCompiler.srcDirs)
            modulePackage.set(this@TaskUsingKotlinCompiler.modulePackage)
            generateDestinationDir.set(this@TaskUsingKotlinCompiler.generateDestinationDir)
        }
    }
}
