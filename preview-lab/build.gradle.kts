plugins {
    alias(libs.plugins.conventionCmpUi)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

cmpConvention {
    moduleBaseName = "preview-lab"
}

kotlin {
    sourceSets {
        val otherWeb by creating {
            dependsOn(commonMain.get())
        }
        listOf(androidMain, jvmMain, iosMain).forEach {
            it.get().dependsOn(otherWeb)
        }

        commonMain.dependencies {
            api(projects.core)
            api(projects.ui)
            api(projects.field)
            implementation(libs.kotlinxSerializationJson)
            implementation(libs.filekitCore)
            implementation(libs.filekitDialogs)
        }
        jvmTest.dependencies {
            implementation(libs.kotestFrameworkEngine)
            implementation(libs.kotestAssertionsCore)
            implementation(libs.kotestRunnerJunit5)
            implementation(libs.kotestProperty)
            implementation(libs.kotlinxCoroutinesTest)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Forward kotest-related system properties to the test JVM
    // See: https://kotest.io/docs/framework/tags.html#gradle
    System.getProperties()
        .filter { it.key.toString().startsWith("kotest.") }
        .forEach { (key, value) -> systemProperty(key.toString(), value) }
}

compose.resources {
    packageOfResClass = "me.tbsten.compose.preview.lab.previewlab.generated.resources"
}

publishConvention {
    artifactName = "PreviewLab"
    artifactId = "preview-lab"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "preview-lab provides PreviewLab composable for displaying and interacting with individual previews including inspector pane, zoom, and screenshot features."
}
