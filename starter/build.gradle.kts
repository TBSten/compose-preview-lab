plugins {
    alias(libs.plugins.conventionKmp)
    alias(libs.plugins.conventionFormat)
    alias(libs.plugins.conventionPublish)
}

kmpConvention {
    moduleBaseName = "starter"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            api(projects.field)
            api(projects.previewLab)
            api(projects.gallery)
        }
    }
}

publishConvention {
    artifactName = "Starter"
    artifactId = "starter"
    description =
        "Compose Preview Lab starter module. " +
        "This module bundles all core modules (core, field, ui, preview-lab, gallery) " +
        "for easy setup. Just add this single dependency to get started with Compose Preview Lab."
}
