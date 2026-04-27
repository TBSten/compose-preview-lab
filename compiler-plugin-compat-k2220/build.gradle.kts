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
    api(projects.compilerPluginCompatK222)
    compileOnly(kotlinCompilerVersion.map { "org.jetbrains.kotlin:kotlin-compiler-embeddable:$it" })
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }
}
