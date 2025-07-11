plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.conventionPublish)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.21")
    implementation(libs.kotlinCompilerEmbeddable)
    implementation(compose.runtime)
}

group = "test"

publishConvention {
    artifactName = "Kotlin Compiler Plugin"
//    artifactId = "compiler-plugin"
    groupId = "test"
    artifactId = "compiler-plugin"
    version = "0.1.0-dev06"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "gradle-plugin facilitates the setup of the KSP Plugin."
}

//
// class MakePreviewPublicCompilerPlugin : KotlinCompilerPluginSupportPlugin {
//    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> =
//        kotlinCompilation.project.provider { emptyList() }
//
//    override fun getCompilerPluginId(): String = "org.jetbrains.compose.compiler.plugins.compose.preview.lab.preview.public"
//
//    override fun getPluginArtifact(): SubpluginArtifact =
//        SubpluginArtifact("org.jetbrains.compose.storytale.preview.public", "local-compiler-plugin")
//
//    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
// //        kotlinCompilation.target.platformType in setOf(
// //            org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm,
// //            org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.wasm,
// //        )
// }
//
// configurations.all {
//    resolutionStrategy.dependencySubstitution {
//        substitute(module("org.jetbrains.compose.storytale:local-compiler-plugin"))
//            .using(project(":modules:compiler-plugin"))
//        substitute(module("org.jetbrains.compose.storytale.preview.public:local-compiler-plugin"))
//            .using(project(":modules:preview-processor"))
//    }
// }

afterEvaluate {
    println(":compiler-plugin: $group:$name:$version")
}
