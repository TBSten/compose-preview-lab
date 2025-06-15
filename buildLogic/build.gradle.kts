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
    }
}

dependencies {
    compileOnly(gradleApi())
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.ktlintGradlePlugin)
}
