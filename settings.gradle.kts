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

fun include(projectPath: String, projectName: String) {
    include(projectPath)
    project(projectPath).apply {
        name = projectName
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
include(":compiler-plugin")
include(":compiler-plugin:compat")
include(":compiler-plugin:compat-k210")
include(":compiler-plugin:compat-k222")
include(":compiler-plugin:compat-k2220")
include(":compiler-plugin:compat-k230")
include(":compiler-plugin:compat-k2320")
include(":compiler-plugin:compat-k2321")
include(":compiler-plugin:compat-k240_beta2")
include(":gradle-plugin")
include(":dokka-plugin")
include(":dokkaDocs")

include(":extension:kotlinx-datetime", projectName = "extension-kotlinx-datetime")
include(":extension:navigation", projectName = "extension-navigation")
// Navigation 3 extension is planned for future support (currently in alpha)
// include(":extension:navigation3", projectName = "extension-navigation3")

// include(":intellij-plugin")

// for dev
include(":dev")
