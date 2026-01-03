enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "composePreviewLabProject"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    // https://github.com/JetBrains/compose-hot-reload?tab=readme-ov-file#set-up-automatic-provisioning-of-the-jetbrains-runtime-jbr-via-gradle
    id("org.gradle.toolchains.foojay-resolver-convention").version("0.10.0")
}

fun include(projectPath: String, `as`: String) {
    include(projectPath)
    project(projectPath).apply {
        name = `as`
    }
}

includeBuild("./buildLogic")
include(":core")
include(":field")
include(":gallery")
include(":preview-lab")
include(":ui")
include(":starter")
include(":annotation")
include(":ksp-plugin")
include(":gradle-plugin")
include(":dokkaDocs")

include(":extension:kotlinx-datetime", `as` = "extension-kotlinx-datetime")

// include(":intellij-plugin")

// for dev
include(":dev")
