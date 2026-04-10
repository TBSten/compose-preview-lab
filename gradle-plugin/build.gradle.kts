import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    `kotlin-dsl`
}

// kotlin-dsl が提供する embedded Kotlin を使うため、
// conventionJvm (libs の kotlinJvm を明示的に適用する) は付与しない。
// conventionJvm が提供していた toolchain / opt-ins 設定を手動で行う。
kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
    compilerOptions {
        optIn.addAll(
            "me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi",
            "me.tbsten.compose.preview.lab.InternalComposePreviewLabApi",
            "me.tbsten.compose.preview.lab.UiComposePreviewLabApi",
        )
    }
}

// kotlin-dsl は language/api version を 1.8 に固定するが、
// Kotlin 2.3+ では 1.8 が廃止されているため明示的に 2.1 以上に上書きする。
// kotlin.compilerOptions では kotlin-dsl の afterEvaluate 設定を
// 上書きできないため、task 単位で設定する。
// https://kotl.in/gradle/kotlin-dsl-version-compatibility
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
    }
}

val generateBuildConfig by tasks.registering {
    val version = libs.versions.composePreviewLab.get()
    val outputDir = layout.buildDirectory.dir("generated/buildConfig/kotlin")
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("me/tbsten/compose/preview/lab/BuildConfig.kt").asFile
        file.parentFile.mkdirs()
        val groupId = "me.tbsten.compose.preview.lab"
        val artifactId = "compiler-plugin"
        file.writeText(
            """
            package me.tbsten.compose.preview.lab

            internal const val PluginGroupId = "$groupId"
            internal const val PluginArtifactId = "$artifactId"
            internal const val PluginVersion = "$version"
            """.trimIndent() + "\n",
        )
    }
}

kotlin.sourceSets.named("main") {
    kotlin.srcDir(generateBuildConfig)
}

dependencies {
    api(projects.annotation)
    compileOnly(gradleApi())
    implementation(libs.kotlinGradlePlugin)
}

gradlePlugin {
    plugins {
        create("compose-preview-lab") {
            id = "me.tbsten.compose.preview.lab"
            implementationClass = "me.tbsten.compose.preview.lab.ComposePreviewLabGradlePlugin"
        }
    }
}

publishConvention {
    artifactName = "Gradle Plugin"
    artifactId = "gradle-plugin"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "gradle-plugin facilitates the setup of the KSP Plugin."
}
