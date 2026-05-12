import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    alias(libs.plugins.kotlinJvm)
    `java-gradle-plugin`
}

// task-034: Gradle 8.13 同梱の `kotlin-dsl` plugin は embedded Kotlin (2.0.21) を要求するが、
// 本プロジェクトは Kotlin 2.3.21 を使うため毎ビルド `WARNING: Unsupported Kotlin plugin version.`
// が `:gradle-plugin` の Configure フェーズで出てしまう。
// Gradle 9 系で embedded Kotlin が更新されれば消える想定だが (task-033 と連動)、それまでの短期 mitigation として
// `kotlin-dsl` plugin を `org.jetbrains.kotlin.jvm` + `java-gradle-plugin` に分解する。
// 本ファイルは precompiled `.gradle.kts` を含まないため `kotlin-dsl` 固有機能には依存していない。
// `gradleApi()` 経由で Gradle Kotlin DSL API は引き続き利用可能。

kotlin {
    jvmToolchain(libs.versions.jvmToolchain.get().toInt())
    compilerOptions {
        optIn.addAll(
            "me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi",
            "me.tbsten.compose.preview.lab.InternalComposePreviewLabApi",
            "me.tbsten.compose.preview.lab.UiComposePreviewLabApi",
        )
    }
}

// Kotlin compile task の language/api version を embedded と歩調を合わせる目的ではなく、
// プロジェクト全体で使う Kotlin 機能の互換 floor を 2.1 に固定する。
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        apiVersion.set(KotlinVersion.KOTLIN_2_1)
        languageVersion.set(KotlinVersion.KOTLIN_2_1)
    }
}

val generateBuildConfig by tasks.registering {
    val version = libs.versions.composePreviewLab.get()
    val outputDir = layout.buildDirectory.dir("generated/buildConfig/kotlin")
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("me/tbsten/compose/preview/lab/BuildConfig.kt").asFile
        file.parentFile.mkdirs()
        val groupId = "me.tbsten.compose.preview.lab"
        val artifactId = "compiler-plugin"
        file.writeText(
            """
            package me.tbsten.compose.preview.lab

            internal const val PluginGroupId = "$groupId"
            internal const val PluginArtifactId = "$artifactId"
            internal const val PluginVersion = "$version"
            """.trimIndent() + "\n",
        )
    }
}

kotlin.sourceSets.named("main") {
    kotlin.srcDir(generateBuildConfig)
}

dependencies {
    api(projects.annotation)
    compileOnly(gradleApi())
    // task-034: `kotlin-dsl` plugin が自動で追加していた Gradle Kotlin DSL API を手動で復元。
    // build script 本体は precompiled script plugin を含まないが、src/ 配下の Kotlin code は
    // `getByType<>()`, `register<>()`, ExtensionAware.property delegate などの拡張関数を利用する。
    compileOnly(gradleKotlinDsl())
    implementation(libs.kotlinGradlePlugin)
}

gradlePlugin {
    plugins {
        create("compose-preview-lab") {
            id = "me.tbsten.compose.preview.lab"
            implementationClass = "me.tbsten.compose.preview.lab.ComposePreviewLabGradlePlugin"
        }
    }
}

publishConvention {
    artifactName = "Gradle Plugin"
    artifactId = "gradle-plugin"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "gradle-plugin facilitates the setup of the KSP Plugin."
}
