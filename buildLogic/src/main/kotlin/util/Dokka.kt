package util

import org.gradle.api.Project
import org.jetbrains.dokka.gradle.DokkaExtension

internal fun Project.dokka(actions: DokkaExtension.() -> Unit) =
    extensions.configure(DokkaExtension::class.java, actions)
