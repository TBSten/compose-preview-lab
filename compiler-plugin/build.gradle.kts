plugins {
    alias(libs.plugins.conventionJvm)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    idea
}

dependencies {
    api(projects.annotation)

    compileOnly(libs.kotlinCompilerEmbeddable)

    testImplementation(libs.kctfork)
    testImplementation(libs.kotestFrameworkEngine)
    testImplementation(libs.kotestAssertionsCore)
    testImplementation(libs.kotestRunnerJunit5)
    testImplementation(libs.kotlinCompilerEmbeddable)
    testImplementation(libs.composeUiToolingPreview)
}

// Kotlin バージョンに応じた source directory を選択
// 2.3+ は kotlin-2.3/ (FirFunction, pluginId), それ以前は kotlin-pre-2.3/ (FirSimpleFunction)
val kotlinVersionStr = libs.versions.kotlin.get()
val kotlinMajor = kotlinVersionStr.substringBefore(".").toIntOrNull() ?: 0
val kotlinMinor = kotlinVersionStr.substringAfter(".").substringBefore(".").toIntOrNull() ?: 0
val activeVersionDir = if (kotlinMajor > 2 || (kotlinMajor == 2 && kotlinMinor >= 3)) "kotlin-2.3" else "kotlin-pre-2.3"
val inactiveVersionDir = if (activeVersionDir == "kotlin-2.3") "kotlin-pre-2.3" else "kotlin-2.3"

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }
    sourceSets["main"].kotlin.srcDir("src/main/$activeVersionDir")
}

// ビルドで使わない方を IDE から除外 (同名クラスの衝突を防ぐ)
idea {
    module {
        excludeDirs = excludeDirs + files("src/main/$inactiveVersionDir")
    }
}

configurations.all {
    resolutionStrategy {
        force(libs.kotlinCompilerEmbeddable.get().toString())
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // kctfork 内部の kotlin-compiler-embeddable が Java 26 の version 文字列を
    // 解析できないため、テスト JVM は Java 21 で実行する
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}

publishConvention {
    artifactName = "Compiler Plugin"
    artifactId = "compiler-plugin"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "compiler-plugin provides a Kotlin Compiler Plugin to collect @Preview."
}
