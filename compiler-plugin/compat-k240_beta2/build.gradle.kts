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
    api(projects.compilerPlugin.compatK230)
    compileOnly(kotlinCompilerVersion.map { "org.jetbrains.kotlin:kotlin-compiler-embeddable:$it" })
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        // kotlin-compiler-embeddable:2.4.0-Beta2 carries Kotlin 2.4 metadata, so compiling it
        // with an older Kotlin Gradle Plugin (2.2.x / 2.3.x) triggers metadata-version mismatch
        // and prerelease warnings.
        // The sources of this compat module are only thin delegates and constructors that target
        // compiler-internal API signatures, so it is safe to skip the metadata / prerelease checks.
        freeCompilerArgs.addAll(
            "-Xskip-prerelease-check",
            "-Xskip-metadata-version-check",
        )
    }
}
