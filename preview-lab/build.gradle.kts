plugins {
    alias(libs.plugins.conventionCmpUi)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

cmpConvention {
    moduleBaseName = "preview-lab"
}

kotlin {
    sourceSets {
        val otherWeb by creating {
            dependsOn(commonMain.get())
        }
        listOf(androidMain, jvmMain, iosMain).forEach {
            it.get().dependsOn(otherWeb)
        }

        // Zoomable ライブラリ対応プラットフォーム用 (JS以外)
        val zoomableMain by creating {
            dependsOn(commonMain.get())
        }
        listOf(androidMain, jvmMain, iosMain, wasmJsMain).forEach {
            it.get().dependsOn(zoomableMain)
        }

        commonMain.dependencies {
            api(projects.core)
            api(projects.ui)
            api(projects.field)
            implementation(libs.kotlinxSerializationJson)
            implementation(libs.filekitCore)
            implementation(libs.filekitDialogs)
        }
        zoomableMain.dependencies {
            implementation(libs.zoomable)
        }
        jvmTest.dependencies {
            implementation(libs.kotestProperty)
            implementation(libs.kotlinxCoroutinesTest)
        }
    }
}

compose.resources {
    packageOfResClass = "me.tbsten.compose.preview.lab.previewlab.generated.resources"
}

publishConvention {
    artifactName = "PreviewLab"
    artifactId = "preview-lab"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "preview-lab provides PreviewLab composable for displaying and interacting with individual previews including inspector pane, zoom, and screenshot features."
}
