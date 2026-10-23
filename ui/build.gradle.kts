plugins {
    alias(libs.plugins.conventionCmpUi)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

cmpConvention {
    moduleBaseName = "ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            // Composables UI (MIT) sources are copied into this module and run on Compose Unstyled 2.6.0.
            // `api`: ThemeProperty/ThemeToken/Theme, LocalContentColor/LocalTextStyle and Unstyled* scope types appear in
            // @UiComposePreviewLabApi signatures and in consumer code (field imports com.composeunstyled.LocalTextStyle).
            api(libs.composeUnstyled)
            api(libs.composeUnstyledTheming)
            implementation(libs.composeUnstyledBuildModifier) // buildModifier {} in Button/Checkbox/Switch/Tabs/RadioGroup
            implementation(libs.composablesRippleIndication) // rememberRippleIndication(color) for theme indications
            // ripple-indication declares material-ripple 1.9.1; this pins material-ripple to the project Compose version.
            implementation(libs.composeMaterialRipple)
        }
        jvmTest.dependencies {
            implementation(libs.kotestProperty)
            implementation(libs.kotlinxCoroutinesTest)
        }
    }
}

compose {
    resources {
        publicResClass = true
        nameOfResClass = "PreviewLabUiRes"
    }
}

publishConvention {
    artifactName = "Ui"
    artifactId = "ui"
    description =
        "A component catalog library that collects and lists @Preview. \n" +
        "By providing APIs such as Field, Event, etc., it provides not only display but also interactive preview.\n" +
        "ui provides shared UI components and theme utilities used across PreviewLab modules."
}

// Ship the MIT notice for the Composables UI-derived sources inside every jar (classes and sources).
tasks.withType<org.gradle.jvm.tasks.Jar>().configureEach {
    from(layout.projectDirectory.file("THIRD_PARTY_NOTICES.md")) {
        into("META-INF")
    }
}
