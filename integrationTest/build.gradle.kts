import kotlinx.validation.ExperimentalBCVApi

plugins {
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.composeMultiplatform).apply(false)
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.hotReload).apply(false)
    id("me.tbsten.compose.preview.lab").apply(false)
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.dokka).apply(false)
}

apiValidation {
    @OptIn(ExperimentalBCVApi::class)
    klib.enabled = true

    nonPublicMarkers.addAll(
        listOf(
            "me.tbsten.compose.preview.lab.InternalComposePreviewLabApi",
            "me.tbsten.compose.preview.lab.UiComposePreviewLabApi",
            // Mirror root build.gradle.kts: experimental signatures are intentionally excluded
            // from BCV baseline so experimental→stable promotions show up as concrete diffs.
            // See root build.gradle.kts for the full Known Limitation note about the
            // `@property:` JVM/Android asymmetric leak.
            "me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi",
        ),
    )

    // Compiler plugin generates classes with environment-dependent hash suffixes
    // (e.g. PreviewLabAutoProvider_<hash>Kt). Exclude this internal package from
    // apiCheck to avoid spurious failures across different build environments.
    ignoredPackages.add("me.tbsten.compose.preview.lab.exports")
}
