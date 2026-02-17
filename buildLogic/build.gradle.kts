plugins {
    alias(libs.plugins.kotlinJvm)
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
        create("jvm") {
            id = "convention-jvm"
            implementationClass = "JvmConventionPlugin"
        }
        create("kmp") {
            id = "convention-kmp"
            implementationClass = "KmpConventionPlugin"
        }
        create("cmp") {
            id = "convention-cmp"
            implementationClass = "CmpConventionPlugin"
        }
        create("cmpUi") {
            id = "convention-cmp-ui"
            implementationClass = "CmpUiConventionPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleApi())
    implementation(libs.kotlinGradlePlugin)
    implementation(libs.ktlintGradlePlugin)
    implementation(libs.vanniktechMavenPublishGradlePlugin)
    implementation(libs.dokkaGradlePlugin)
    implementation(libs.androidGradlePlugin)
    implementation(libs.composeGradlePlugin)
    implementation(libs.hotReloadGradlePlugin)
}
