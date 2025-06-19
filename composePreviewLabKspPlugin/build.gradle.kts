plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

dependencies {
    implementation(libs.kspApi)
    implementation(libs.kotlinCompilerEmbeddable)

    implementation(libs.kotlinxSerializationJson)
}

publishConvention {
    artifactId = "ksp-plugin"
}
