plugins {
    id("convention-jvm")
    id("convention-format")
}

dependencies {
    compileOnly(libs.kotlinCompilerEmbeddable)

    testImplementation(libs.kotestFrameworkEngine)
    testImplementation(libs.kotestAssertionsCore)
    testImplementation(libs.kotestRunnerJunit5)
    testImplementation(libs.kotestProperty)
    testImplementation(libs.kotlinCompilerEmbeddable)
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // kctfork bundles a kotlin-compiler-embeddable that cannot parse Java 26 version
    // strings, so the test JVM has to run on Java 21.
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}
