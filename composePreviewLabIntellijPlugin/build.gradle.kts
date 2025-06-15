plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.intellijPlatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.conventionFormat)
}

repositories {
    intellijPlatform {
        google()
        defaultRepositories()
    }
    mavenCentral()
    maven("https://packages.jetbrains.team/maven/p/kpm/public/") // for jewel
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
    }
    api(libs.jewelIdeLafBridge)
    api(projects.composePreviewLab) {
        exclude(group = "org.jetbrains.kotlinx")
    }
    api(compose.foundation) {
        exclude(group = "org.jetbrains.kotlinx")
    }
    api(compose.material3) {
        exclude(group = "org.jetbrains.kotlinx")
    }
    api(compose.desktop.currentOs) {
        exclude(group = "org.jetbrains.compose.material")
        exclude(group = "org.jetbrains.kotlinx")
    }
}
