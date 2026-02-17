plugins {
    alias(libs.plugins.conventionJvm)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    `kotlin-dsl`
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

// Generate version resource for the compiler plugin artifact resolution
val generateVersionResource by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/resources/version")
    val version = project.version.toString()
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("compose-preview-lab-version.txt").asFile
        file.parentFile.mkdirs()
        file.writeText(version)
    }
}

sourceSets.main {
    resources.srcDir(generateVersionResource)
}

publishConvention {
    artifactName = "Gradle Plugin"
    artifactId = "gradle-plugin"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "gradle-plugin configures the Kotlin Compiler Plugin for preview collection."
}
