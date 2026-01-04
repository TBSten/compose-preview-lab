plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.composeCompiler).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.hotReload).apply(false)
    alias(libs.plugins.ksp).apply(false)
    id("me.tbsten.compose.preview.lab").apply(false)
}
