package util

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

internal fun Project.ktlint(action: KtlintExtension.() -> Unit) = extensions.configure(action)
