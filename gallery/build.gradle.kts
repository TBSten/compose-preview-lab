import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    alias(libs.plugins.conventionCmpUi)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

cmpConvention {
    moduleBaseName = "gallery"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(projects.ui)
            api(projects.field)
            api(projects.previewLab)
        }
        jvmTest.dependencies {
            implementation(libs.kotestFrameworkEngine)
            implementation(libs.kotestAssertionsCore)
            implementation(libs.kotestRunnerJunit5)
            implementation(libs.kotestProperty)
            implementation(libs.kotlinxCoroutinesTest)
        }
        jvmMain.dependencies {
            implementation(libs.ktorServerCore)
            implementation(libs.ktorServerCio)
            implementation(libs.mcpKotlinSdk)
            implementation(libs.slf4jNop)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Forward kotest-related system properties to the test JVM
    // See: https://kotest.io/docs/framework/tags.html#gradle
    System.getProperties()
        .filter { it.key.toString().startsWith("kotest.") }
        .forEach { (key, value) -> systemProperty(key.toString(), value) }
}

publishConvention {
    artifactName = "Gallery"
    artifactId = "gallery"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "gallery provides PreviewLabGallery UI for browsing and navigating through collected previews in a tree structure."
}

buildkonfig {
    packageName = "me.tbsten.compose.preview.lab.gallery"

    defaultConfigs {
        buildConfigField(STRING, "COMPOSE_PREVIEW_LAB_VERSION", libs.versions.composePreviewLab.get())
    }
}
