import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.hotReload)
    alias(libs.plugins.ksp)
    alias(libs.plugins.buildkonfig)
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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("me.tbsten.compose.preview.lab:starter:${libs.versions.composePreviewLab.get()}")
            implementation(project(":uiLib"))
            implementation(project(":helloComposePreviewLab"))

            // TODO migrate retain { } (compose runtime api)
            implementation("io.github.takahirom.rin:rin:0.3.0")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.desktop.uiTestJUnit4)
            implementation(libs.kotestFrameworkEngine)
            implementation(libs.kotestAssertionsCore)
            implementation(libs.kotestRunnerJunit5)
            implementation(libs.kotestProperty)
            implementation(libs.androidxLifecycleViewmodel)
            implementation(libs.androidxLifecycleRuntimeCompose)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        jsMain.dependencies {
            implementation(compose.html.core)
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
    androidTestImplementation(libs.androidx.uitest.junit4)
    debugImplementation(libs.androidx.uitest.testManifest)

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
    systemProperty("kotest.tags", System.getProperty("kotest.tags") ?: "")
}
