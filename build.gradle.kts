plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.jvm).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.hotReload).apply(false)
}

subprojects {
    group = "me.tbsten.compose.preview.lab"
//    version = libs.versions.composePreviewLab
    version = "0.1.0"
}