plugins {
    alias(libs.plugins.jvm)
}

dependencies {
    implementation(libs.kspApi)
    implementation(libs.kotlinCompilerEmbeddable)
}
