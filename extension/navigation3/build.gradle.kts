plugins {
    alias(libs.plugins.conventionCmp)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

cmpConvention {
    moduleBaseName = "extension-navigation3"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeFoundation)
            implementation(libs.composeUi)
            implementation(projects.ui)
            implementation(projects.field)
            implementation(libs.androidxNavigation3Ui)
            implementation(libs.kotlinxSerializationCore)
        }
    }
}

publishConvention {
    artifactName = "Extension Navigation 3"
    artifactId = "extension-navigation3"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "extension-navigation3 provides extension functions for Navigation 3."
}
