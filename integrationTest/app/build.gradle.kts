import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.hotReload)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.kotlinxSerialization)
    id("me.tbsten.compose.preview.lab")
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        @Suppress("OPT_IN_USAGE")
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    jvm()

    js {
        browser()
        binaries.library()
        binaries.executable()
        generateTypeScriptDefinitions()

        compilerOptions {
            target = "es2015"
        }
    }

    @Suppress("OPT_IN_USAGE")
    wasmJs {
        browser()
        binaries.executable()
        binaries.library()
        generateTypeScriptDefinitions()

        compilerOptions {
            target = "es2015"
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeRuntime)
            implementation(libs.composeFoundation)
            implementation(libs.composeMaterial3)
            implementation(libs.composeComponentsResources)
            implementation(libs.composeUiToolingPreview)
            implementation("me.tbsten.compose.preview.lab:starter:${libs.versions.composePreviewLab.get()}")

            implementation("me.tbsten.compose.preview.lab:extension-kotlinx-datetime:${libs.versions.composePreviewLab.get()}")
            implementation(libs.kotlinxDatetime)

            implementation("me.tbsten.compose.preview.lab:extension-navigation:${libs.versions.composePreviewLab.get()}")
            implementation(libs.androidxNavigation)
            implementation(libs.kotlinxSerializationCore)

            implementation(project(":uiLib"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.composeUiTest)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.composeUiTestJunit4)
            implementation(libs.kotestFrameworkEngine)
            implementation(libs.kotestAssertionsCore)
            implementation(libs.kotestRunnerJunit5)
            implementation(libs.kotestProperty)
            implementation(libs.androidxLifecycleViewmodel)
            implementation(libs.androidxLifecycleRuntimeCompose)
        }

        androidMain.dependencies {
            implementation(libs.composeUiTooling)
            implementation(libs.androidxActivityCompose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab.sample"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        targetSdk = 36

        applicationId = "me.tbsten.compose.preview.lab.sample.androidApp"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

// https://developer.android.com/develop/ui/compose/testing#setup
dependencies {
    androidTestImplementation(libs.androidxUitestJunit4)
    debugImplementation(libs.androidxUitestTestManifest)

    val composePreviewLabKspPlugin =
        "me.tbsten.compose.preview.lab:ksp-plugin:${libs.versions.composePreviewLab.get()}"
    add("kspCommonMainMetadata", composePreviewLabKspPlugin)
    ksp(composePreviewLabKspPlugin)
    add("kspAndroid", composePreviewLabKspPlugin)
    add("kspJvm", composePreviewLabKspPlugin)
    add("kspJs", composePreviewLabKspPlugin)
    add("kspWasmJs", composePreviewLabKspPlugin)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Compose Preview Lab"
            packageVersion = "1.0.0"

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
            }
            macOS {
                iconFile.set(project.file("desktopAppIcons/MacosIcon.icns"))
                bundleID = "me.tbsten.compose.preview.lab.sample.desktopApp"
            }
        }
    }
}

composePreviewLab {
    generateFeaturedFiles = true
}

buildkonfig {
    packageName = "me.tbsten.compose.preview.lab.sample"

    defaultConfigs {
        val iterations = findProperty("test.property.iterations")?.toString()?.toIntOrNull() ?: 20
        buildConfigField(INT, "PROPERTY_TEST_ITERATIONS", iterations.toString())
    }
}

tasks.register<ComposeHotRun>("runHot") {
    mainClass.set("MainKt")
}

composePreviewLab {
    generateFeaturedFiles = true
}

val cleanPreviewLabGallery by tasks.registering(Delete::class) {
    delete(layout.buildDirectory.dir("compose-preview-lab-gallery"))
}

/**
 * Web ドキュメント用に Dokka バージョンを TypeScript ファイルとして出力するタスク。
 */
val generateWebDocsLibVersion by tasks.registering {
    // integrationTest プロジェクトのルート（= integrationTest/）
    val integrationTestRoot = rootProject.layout.projectDirectory
    // integrationTest/web/src/generated/kdocVersion.ts というファイルを生成する
    val outputFile =
        integrationTestRoot
            .dir("web/src/generated")
            .file("libVersion.ts")

    // ルートの libs.versions.composePreviewLab をそのまま利用する
    val version = rootProject.libs.versions.composePreviewLab.get()

    outputs.file(outputFile)

    doLast {
        val file = outputFile.asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            // This file is generated by :app:generateWebDocsLibVersion.
            // DO NOT EDIT MANUALLY.
            export const composePreviewLabVersion = "$version";
            """.trimIndent(),
        )
    }
}

val buildDevelopmentPreviewLabGallery by tasks.registering(Copy::class) {
    dependsOn("jsBrowserDevelopmentExecutableDistribution")
    dependsOn(cleanPreviewLabGallery)
    dependsOn(generateWebDocsLibVersion)

    from(layout.buildDirectory.dir("dist/js/developmentExecutable"))
    into(layout.buildDirectory.dir("web-static-content/compose-preview-lab-gallery"))
}

val buildProductionPreviewLabGallery by tasks.registering(Copy::class) {
    dependsOn("jsBrowserDistribution")
    dependsOn(cleanPreviewLabGallery)
    dependsOn(generateWebDocsLibVersion)

    from(layout.buildDirectory.dir("dist/js/productionExecutable"))
    into(layout.buildDirectory.dir("web-static-content/compose-preview-lab-gallery"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Forward kotest-related system properties to the test JVM
    // See: https://kotest.io/docs/framework/tags.html#gradle
    System.getProperties()
        .filter { it.key.toString().startsWith("kotest.") }
        .forEach { (key, value) -> systemProperty(key.toString(), value) }
}
