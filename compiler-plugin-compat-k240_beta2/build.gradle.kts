plugins {
    id("convention-jvm")
    id("convention-format")
}

val kotlinCompilerVersion = providers
    .fileContents(layout.projectDirectory.file("version.txt"))
    .asText
    .map { it.trim() }

dependencies {
    api(projects.compilerPlugin.compat)
    api(projects.compilerPluginCompatK230)
    compileOnly(kotlinCompilerVersion.map { "org.jetbrains.kotlin:kotlin-compiler-embeddable:$it" })
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        // kotlin-compiler-embeddable:2.4.0-Beta2 は Kotlin 2.4 metadata を持つため、
        // 旧 Kotlin Gradle Plugin (2.2.x / 2.3.x) で compile すると metadata version
        // mismatch / prerelease 警告が発生する。
        // Compat module のソースは compiler 内部 API のシグネチャに対する単純な委譲・
        // 構築のみなので、metadata check / prerelease check は安全に skip できる。
        freeCompilerArgs.addAll(
            "-Xskip-prerelease-check",
            "-Xskip-metadata-version-check",
        )
    }
}
