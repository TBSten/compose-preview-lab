---
title: "Build Settings"
sidebar_position: 7
---

# Build Settings

The Compose Preview Lab Gradle plugin exposes **build settings** that control code generation
and how previews are collected. This page covers the most common options you can set via the
`composePreviewLab { ... }` block and the equivalent `gradle.properties` keys.

:::info Where do I put this?
The `composePreviewLab { ... }` block lives in the `build.gradle.kts` of any module that
applies the Compose Preview Lab Gradle plugin:

```kotlin
plugins {
    id("me.tbsten.compose.preview.lab") version "<version>"
}

composePreviewLab {
    // settings go here
}
```
:::

## 1. generatePackage

Controls the package name used for generated files such as `FeaturedFileList`. Defaults to
a camelCase form of the Gradle project name (e.g. `my-app` → `myApp`).

```kotlin title="build.gradle.kts"
composePreviewLab {
    generatePackage = "com.example.preview"
}
```

:::tip When to override
Set this explicitly when you want generated files to land under a specific package — for
example, to keep them aligned with the rest of your module's package layout.
:::

## 2. generateFeaturedFiles

Controls whether the plugin scans `.composepreviewlab/featured/` and emits a
`FeaturedFileList`. See [Featured Files](./featured-files) for the full story.

Defaults to `false`.

```kotlin
composePreviewLab {
    // Only enable when you actually use the Featured Files feature.
    generateFeaturedFiles = true
}
```

:::tip Typical Featured Files workflow
- Put one text file per group under `.composepreviewlab/featured/`
- Enable `generateFeaturedFiles = true`
- Pass the generated `FeaturedFileList` to `previewLabApplication(...)`
:::

## 3. Configuring via gradle.properties

Every option you can set in the `composePreviewLab { ... }` block can also be set via
**`gradle.properties`** (or the `-PcomposePreviewLab.*=...` CLI flag). This is handy for
monorepos that want a single baseline configuration applied to every module, or for CI
pipelines that want to override values without editing build scripts.

### Key reference

| `gradle.properties` key | DSL property | Type |
| --- | --- | --- |
| `composePreviewLab.generatePackage` | `generatePackage` | String |
| `composePreviewLab.projectRootPath` | `projectRootPath` | String |
| `composePreviewLab.generateFeaturedFiles` | `generateFeaturedFiles` | Boolean |
| `composePreviewLab.collectPreviews.enabled` | `collectPreviews.enabled` | Boolean |
| `composePreviewLab.collectPreviews.defaultCollectScope` | `collectPreviews.defaultCollectScope` | String |

Boolean values are recognized as `true` / `false` (case-insensitive). Any other value
falls back to the built-in default instead of failing the build.

### Precedence

Values are resolved in three steps, with **higher entries winning**:

1. `composePreviewLab { ... }` DSL block (explicit setting)
2. `gradle.properties` (or `-PcomposePreviewLab.*=...` on the command line)
3. Plugin built-in default

This matches the Gradle convention used by properties like `org.gradle.parallel`: define a
baseline in `gradle.properties`, then override per module via the DSL when needed.

### Examples

```properties title="gradle.properties"
# Baseline applied to every module
composePreviewLab.generateFeaturedFiles=true
composePreviewLab.collectPreviews.defaultCollectScope=acme_ui
```

```bash title="One-off CLI override"
./gradlew assemble -PcomposePreviewLab.generateFeaturedFiles=true
```

### Unknown-key detection

To catch typos early, the plugin emits a single consolidated warning at configuration time
whenever it finds an **unknown key** under the `composePreviewLab.*` prefix:

```
[compose-preview-lab] Unknown gradle.properties key(s) under 'composePreviewLab.*' detected:
composePreviewLab.generatePackge. Known keys are: composePreviewLab.collectPreviews.defaultCollectScope,
composePreviewLab.collectPreviews.enabled, composePreviewLab.generateFeaturedFiles,
composePreviewLab.generatePackage, composePreviewLab.projectRootPath.
```

This is intentionally a warning (not a build failure) so a typo in a third-party
properties file cannot break your build. Treat it as actionable and fix the key in
`gradle.properties` when you see it.
