plugins {
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
    alias(libs.plugins.conventionJvm)
}

dependencies {
    compileOnly(libs.dokkaCore)
    implementation(libs.dokkaBase)
    implementation(libs.kotlinxHtml)

    testImplementation(libs.dokkaCore)
    testImplementation(libs.dokkaBase)
    testImplementation(libs.dokkaTestApi)
    testImplementation(libs.dokkaBaseTestUtils)
    testImplementation(libs.dokkaAnalysisKotlinSymbols)
    testImplementation(libs.jsoup)
    testImplementation(libs.junitJupiter)
    testImplementation(libs.kotestAssertionsCore)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

publishConvention {
    artifactName = "Dokka Plugin"
    artifactId = "dokka-plugin"
    description =
        "Dokka plugin for Compose Preview Lab. " +
        "Embeds PreviewLab previews into generated KDoc via the @previewLab tag."
}
