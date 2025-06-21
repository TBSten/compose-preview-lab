plugins {
    alias(libs.plugins.jvm)
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("format") {
            id = "convention-format"
            implementationClass = "FormatConventionPlugin"
        }
        create("publish") {
            id = "convention-publish"
            implementationClass = "PublishConventionPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleApi())
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.ktlintGradlePlugin)
    implementation(libs.vanniktechMavenPublishGradlePlugin)
    implementation(libs.dokkaGradlePlugin)
}
