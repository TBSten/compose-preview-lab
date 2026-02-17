plugins {
    alias(libs.plugins.conventionCmp)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

cmpConvention {
    moduleBaseName = "extension-kotlinx-datetime"
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeFoundation)
            implementation(libs.composeUi)
            implementation(projects.ui)
            implementation(projects.field)
            implementation(libs.kotlinxDatetime)
            implementation(libs.kotlinxSerializationCore)
        }
    }
}

publishConvention {
    artifactName = "Extension Kotlinx DateTime"
    artifactId = "extension-kotlinx-datetime"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "extension-kotlinx-datetime provides extension functions for kotlinx.datetime."
}
