@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.buildkonfig)
    id("me.tbsten.compose.preview.lab")
    alias(libs.plugins.hotReload)
}

kotlin {
    jvmToolchain(17)

    // for Preview
    androidTarget {
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

    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeRuntime)
            implementation(libs.composeFoundation)
            implementation(libs.composeMaterial3)
            implementation(libs.composeUiToolingPreview)
            implementation("me.tbsten.compose.preview.lab:starter:${libs.versions.composePreviewLab.get()}")

            // kotlinx-datetime extension
            implementation("me.tbsten.compose.preview.lab:extension-kotlinx-datetime:${libs.versions.composePreviewLab.get()}")
            implementation(libs.kotlinxDatetime)

            // navigation extension
            implementation("me.tbsten.compose.preview.lab:extension-navigation:${libs.versions.composePreviewLab.get()}")
            implementation(libs.androidxNavigation)
            implementation(libs.kotlinxSerializationCore)
        }

        androidMain.dependencies {
            implementation(libs.composeUiTooling)
            implementation(libs.androidxActivityCompose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
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
    }
}

android {
    namespace = "me.tbsten.compose.preview.lab.docs"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        targetSdk = 36

        applicationId = "me.tbsten.compose.preview.lab.docs.androidApp"
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    androidTestImplementation(libs.androidxUitestJunit4)
    debugImplementation(libs.androidxUitestTestManifest)

    val composePreviewLabKspPlugin =
        "me.tbsten.compose.preview.lab:ksp-plugin:${libs.versions.composePreviewLab.get()}"
    add("kspCommonMainMetadata", composePreviewLabKspPlugin)
    add("kspJvm", composePreviewLabKspPlugin)
    add("kspJs", composePreviewLabKspPlugin)
}

tasks.register<ComposeHotRun>("runHot") {
    mainClass.set("MainKt")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Forward kotest-related system properties to the test JVM
    // See: https://kotest.io/docs/framework/tags.html#gradle
    System.getProperties()
        .filter { it.key.toString().startsWith("kotest.") }
        .forEach { (key, value) -> systemProperty(key.toString(), value) }
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

tasks.withType<Test> {
    useJUnitPlatform()
    // Forward kotest-related system properties to the test JVM
    // See: https://kotest.io/docs/framework/tags.html#gradle
    System.getProperties()
        .filter { it.key.toString().startsWith("kotest.") }
        .forEach { (key, value) -> systemProperty(key.toString(), value) }
}

val cleanPreviewLabGallery by tasks.registering(Delete::class) {
    delete(layout.buildDirectory.dir("compose-preview-lab-gallery"))
}

/**
 * Web ドキュメント用に Compose Preview Lab バージョンを TypeScript ファイルとして出力するタスク。
 */
val generateWebDocsLibVersion by tasks.registering {
    // docs プロジェクトのルート（= docs/）
    val docsRoot = rootProject.layout.projectDirectory
    // docs/web/src/generated/libVersion.ts というファイルを生成する
    val outputFile =
        docsRoot
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
            // This file is generated by :sample:generateWebDocsLibVersion.
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
