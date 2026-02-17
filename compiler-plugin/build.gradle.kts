plugins {
    alias(libs.plugins.conventionJvm)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    compileOnly(libs.kotlinCompilerEmbeddable)
    api(projects.annotation)
    implementation(libs.kotlinxSerializationJson)
}

publishConvention {
    artifactName = "Compiler Plugin"
    artifactId = "compiler-plugin"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "compiler-plugin provides a Kotlin Compiler Plugin to collect @Preview."
}
