plugins {
    alias(libs.plugins.jvm)
    `java-gradle-plugin`
    `kotlin-dsl`
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(libs.kotlinGradlePlugin)
    compileOnly(libs.kotlinGradlePluginApi)
    implementation(libs.kotlinCompilerEmbeddable)
}

gradlePlugin {
    plugins {
        create("compose-preview-lab") {
            id = "me.tbsten.compose.preview.lab"
            implementationClass =
                "me.tbsten.compose.preview.lab.gradle.plugin.ComposePreviewLabPlugin"
            displayName = "Compose Preview Lab Gradle Plugin"
        }
    }
}
