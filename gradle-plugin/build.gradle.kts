plugins {
    alias(libs.plugins.conventionJvm)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    `kotlin-dsl`
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
