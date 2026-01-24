import kotlinx.validation.ExperimentalBCVApi

plugins {
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinJvm).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.composeMultiplatform).apply(false)
    alias(libs.plugins.lumoUi)
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.hotReload).apply(false)
    alias(libs.plugins.vanniktechMavenPublish).apply(false)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binaryCompatibilityValidator)
}

allprojects {
    group = "me.tbsten.compose.preview.lab"
    version = rootProject.libs.versions.composePreviewLab.get()
}

apiValidation {
    @OptIn(ExperimentalBCVApi::class)
    klib.enabled = true
    ignoredProjects.add(
        projects.dev.name,
    )
    nonPublicMarkers.addAll(
        listOf(
            "me.tbsten.compose.preview.lab.InternalComposePreviewLabApi",
            "me.tbsten.compose.preview.lab.UiComposePreviewLabApi",
        ),
    )
}

val logVersion by tasks.registering {
    val version = libs.versions.composePreviewLab
    doLast {
        println(version.get())
    }
}
