plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(libs.kspApi)
    implementation(libs.kotlinCompilerEmbeddable)

    implementation(libs.kotlinxSerializationJson)
}
