plugins {
    alias(libs.plugins.conventionCmp)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

cmpConvention {
    moduleBaseName = "extension-navigation"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeFoundation)
            implementation(libs.composeUi)
            implementation(libs.composeComponentsResources)
            implementation(projects.ui)
            implementation(projects.field)
            implementation(libs.androidxNavigation)
            implementation(libs.kotlinxSerializationCore)
        }
    }
}

publishConvention {
    artifactName = "Extension Navigation"
    artifactId = "extension-navigation"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "extension-navigation provides NavControllerField for inspecting and controlling NavHostController in PreviewLab."
}
