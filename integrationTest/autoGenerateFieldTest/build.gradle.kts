plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
    id("me.tbsten.compose.preview.lab")
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    jvm()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation("me.tbsten.compose.preview.lab:annotation:${libs.versions.composePreviewLab.get()}")
            implementation("me.tbsten.compose.preview.lab:field:${libs.versions.composePreviewLab.get()}")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab.test.autogenerate"
    compileSdk = 36
    defaultConfig { minSdk = 23 }
}

dependencies {
    val kspPlugin = "me.tbsten.compose.preview.lab:ksp-plugin:${libs.versions.composePreviewLab.get()}"
    add("kspCommonMainMetadata", kspPlugin)
}

// KSP Multiplatform Workaround
// Enables KSP-generated code in commonMain for multiplatform projects
fun Project.setupKspForMultiplatformWorkaround() {
    kotlin.sourceSets.commonMain {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
    tasks.configureEach {
        if (name.startsWith("ksp") && name != "kspCommonMainKotlinMetadata") {
            dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
            enabled = false
        }
    }
}
setupKspForMultiplatformWorkaround()

composePreviewLab {
    generateAutoField = true
    generatePreviewList = false
    generatePreviewAllList = false
    generateFeaturedFiles = false
}
