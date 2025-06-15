plugins {
    alias(libs.plugins.jvm)
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("jvm") {
            id = "convention-jvm"
            implementationClass = "ConventionJvmPlugin"
        }
    }
}

dependencies {
    compileOnly(gradleApi())
    implementation(libs.kotlinGradlePlugin)
}
