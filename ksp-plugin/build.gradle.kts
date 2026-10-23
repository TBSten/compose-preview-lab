plugins {
    alias(libs.plugins.conventionJvm)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

dependencies {
    api(projects.annotation)
    implementation(libs.kspApi)
}

publishConvention {
    artifactName = "KSP Plugin"
    artifactId = "ksp-plugin"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "ksp-plugin is an opt-in KSP processor that generates PreviewLabField factories for @AutoGenerateField."
}
