plugins {
    id("convention-jvm")
    id("convention-format")
}

dependencies {
    compileOnly(libs.kotlinCompilerEmbeddable)

    testImplementation(libs.kotestFrameworkEngine)
    testImplementation(libs.kotestAssertionsCore)
    testImplementation(libs.kotestRunnerJunit5)
    testImplementation(libs.kotlinCompilerEmbeddable)
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
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
