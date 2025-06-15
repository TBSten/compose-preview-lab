plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
}

dependencies {
    implementation(libs.kspApi)
    implementation(libs.kotlinCompilerEmbeddable)

    implementation(libs.kotlinxSerializationJson)
}
