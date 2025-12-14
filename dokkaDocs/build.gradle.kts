plugins {
    id("org.jetbrains.dokka")
}

dokka {
    dokkaSourceSets.configureEach {
        sourceLink {
            // Read docs for more details: https://kotlinlang.org/docs/dokka-gradle.html#source-link-configuration
            remoteUrl("https://github.com/TBSten/compose-preview-lab/tree/main")
            localDirectory.set(rootDir)
        }
    }
}

dependencies {
    dokka(projects.core)
    dokka(projects.field)
    dokka(projects.gallery)
    dokka(projects.previewLab)
    dokka(projects.ui)
    dokka(projects.annotation)
    dokka(projects.testing)
}

dokka {
    moduleName.set("Compose Preview Lab")
}
