plugins {
    alias(libs.plugins.conventionCmp)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

cmpConvention {
    moduleBaseName = "extension-debugger"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeFoundation)
            implementation(libs.composeUi)
            implementation(libs.composeComponentsResources)
            implementation(projects.ui)
            implementation(projects.field)
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(projects.field)
            implementation(libs.kotestFrameworkEngine)
            implementation(libs.kotestAssertionsCore)
            implementation(libs.kotestRunnerJunit5)
            implementation(libs.kotlinxCoroutinesTest)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    System.getProperties()
        .filter { it.key.toString().startsWith("kotest.") }
        .forEach { (key, value) -> systemProperty(key.toString(), value) }
}

publishConvention {
    artifactName = "Extension Debugger"
    artifactId = "extension-debugger"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "extension-debugger provides debug menu utilities using Field API."
}
