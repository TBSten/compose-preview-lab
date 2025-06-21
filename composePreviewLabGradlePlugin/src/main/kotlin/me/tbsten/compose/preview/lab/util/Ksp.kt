package me.tbsten.compose.preview.lab.util

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

internal val Project.ksp get() = extensions.getByType<KspExtension>()
internal operator fun KspExtension.invoke(action: KspExtension.() -> Unit) = apply(action)
