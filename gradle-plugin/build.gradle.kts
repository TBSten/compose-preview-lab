plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    `kotlin-dsl`
}

dependencies {
    compileOnly(gradleApi())
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.kotlinGradlePluginApi)
    implementation(libs.kspGradlePlugin)
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
