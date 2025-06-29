plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.jvm).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.lumoUi)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.hotReload).apply(false)
    alias(libs.plugins.vanniktechMavenPublish).apply(false)
    alias(libs.plugins.dokka)
}

allprojects {
    group = "me.tbsten.compose.preview.lab"
    version = rootProject.libs.versions.composePreviewLab.get()
}
