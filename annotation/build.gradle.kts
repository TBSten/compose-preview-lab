plugins {
    alias(libs.plugins.conventionKmp)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

kmpConvention {
    moduleBaseName = "annotation"
}

publishConvention {
    artifactName = "Annotation"
    artifactId = "annotation"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "annotation provides annotations used in runtime and tooling"
}
