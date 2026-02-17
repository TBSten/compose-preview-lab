plugins {
    alias(libs.plugins.conventionJvm)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

dependencies {
    api(projects.annotation)

    implementation(libs.kspApi)
    implementation(libs.kotlinCompilerEmbeddable)

    implementation(libs.kotlinxSerializationJson)
}

publishConvention {
    artifactName = "KSP Plugin"
    artifactId = "ksp-plugin"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "ksp-plugin provides a KSP Plugin to collect @Preview."
}
