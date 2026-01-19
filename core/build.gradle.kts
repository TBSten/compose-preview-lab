plugins {
    alias(libs.plugins.conventionCmpUi)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

cmpConvention {
    moduleBaseName = "core"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.annotation)
        }

        val otherJsMain by creating {
            dependsOn(commonMain.get())
        }
        listOf(
            androidMain,
            iosMain,
            jvmMain,
            wasmJsMain,
        ).forEach {
            it.get().dependsOn(otherJsMain)
        }
    }
}

publishConvention {
    artifactName = "Core"
    artifactId = "core"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "core provides runtime APIs such as annotations and gallery UI needed to collect Previews."
}
