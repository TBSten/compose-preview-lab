import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    alias(libs.plugins.conventionJvm)
    `java-gradle-plugin`
}

// プロジェクト全体で使う Kotlin 機能の互換 floor を 2.1 に固定する。
// gradle-plugin はユーザの Gradle (embedded Kotlin が古い可能性あり) 上で動作するため、
// 新しめの Kotlin 言語機能を取り込まないよう apiVersion/languageVersion を明示的に固定する。
// convention-jvm は language/api version を設定しないので、ここで個別に設定する。
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
    compileOnly(gradleKotlinDsl())
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
