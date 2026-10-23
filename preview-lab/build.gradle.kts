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

        // Zoomable ライブラリ対応プラットフォーム用 (JS 以外)。
        // net.engawapg.lib:zoomable は 2.15.0 で JS target を追加したが、同時に Compose 1.12.1 を
        // 要求するため、このリポジトリの Compose バージョンが追いつくまでは 2.11.1 (Compose 1.10.0) に
        // 留め、JS は従来の draggable2D + graphicsLayer 実装 (jsMain) を維持する。
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
            implementation(libs.kotestFrameworkEngine)
            implementation(libs.kotestAssertionsCore)
            implementation(libs.kotestRunnerJunit5)
            implementation(libs.kotestProperty)
            implementation(libs.kotlinxCoroutinesTest)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Forward kotest-related system properties to the test JVM
    // See: https://kotest.io/docs/framework/tags.html#gradle
    System.getProperties()
        .filter { it.key.toString().startsWith("kotest.") }
        .forEach { (key, value) -> systemProperty(key.toString(), value) }
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
