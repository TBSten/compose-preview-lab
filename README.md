[FIXME WIP DOCUMENTATION]

# Compose Preview Lab

<img src="./docs/cover.png" width="1024" />

<p align="center">
<a href="./README.md">English</a>
 |
<a href="./README.ja.md">日本語</a>
 |
<a href="https://tbsten.github.io/compose-preview-lab/integrationTest/main/js/">Sample</a>
 |
<a href="https://deepwiki.com/TBSten/compose-preview-lab">DeepWiki</a>
</p>

> [!IMPORTANT]
> This project is still a work in progress, and its API is unstable and may change without any
> notice. Using this plugin for a hobby project is fine, but we do not recommend using it for
> production projects yet.

Compose Preview Lab turns @Preview into an interactive Component Playground.
You can pass parameters to components, enabling more than just static snapshots—making manual testing easier and helping new
developers understand components faster.
Compose Multiplatform is supported.

## Try online

- [Online Sample](https://tbsten.github.io/compose-preview-lab/integrationTest/main/js/)

## Setup

<details>
<summary> [Recommended] Compose Multiplatform Project</summary>

Please set up the following for all modules for which you want to collect `@Preview` using Compose
Preview Lab.

<a href="https://central.sonatype.com/artifact/me.tbsten.compose.preview.lab/core">
<img src="https://img.shields.io/maven-central/v/me.tbsten.compose.preview.lab/core?label=compose-preview-lab" alt="Maven Central"/>
</a>
<a href="https://central.sonatype.com/artifact/com.google.devtools.ksp/symbol-processing-api">
<img src="https://img.shields.io/maven-central/v/com.google.devtools.ksp/symbol-processing-api?label=ksp" alt="KSP Version"/>
</a>

```kts
plugins {
    // ⭐️ Add KSP for collect `@Preview`
    id("com.google.devtools.ksp") version "<ksp-version>"
    // ⭐️ Add Compose Preview Lab Gradle plugin
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // ⭐️ Add Compose Preview Lab core artifact
            implementation("me.tbsten.compose.preview.lab:core:<compose-preview-lab-version>")
        }
    }
}

dependencies {
    // ⭐️ Add Compose Preview Lab KSP plugin
    val composePreviewLabKspPlugin =
        "me.tbsten.compose.preview.lab:ksp-plugin:<compose-preview-lab-version>"
    add("kspCommonMainMetadata", composePreviewLabKspPlugin)
    // each platform
    add("kspAndroid", composePreviewLabKspPlugin)
    add("kspJvm", composePreviewLabKspPlugin)
    add("kspJs", composePreviewLabKspPlugin)
    add("kspWasmJs", composePreviewLabKspPlugin)
    // iOS targets (if needed)
    // add("kspIosX64", composePreviewLabKspPlugin)
    // add("kspIosArm64", composePreviewLabKspPlugin)
    // add("kspIosSimulatorArm64", composePreviewLabKspPlugin)
}
```

</details>

<details>
<summary> Android Project </summary>

> 🚨 WARNING
>
> Pure Android projects (projects that do not use the Kotlin Multiplatform) can also use
> the Compose Preview Lab, but their functionality is severely limited,
> such as not being able to browse on the
> web, and it may be difficult to see the benefits of the Compose Preview Lab. However, the
> Consider using Compose Multiplatform even if your project is Android-only.
> I believe that this concept is not limited to Compose Preview Lab, but should be the norm for all
> projects using Compose in the future.

<a href="https://central.sonatype.com/artifact/me.tbsten.compose.preview.lab/core">
<img src="https://img.shields.io/maven-central/v/me.tbsten.compose.preview.lab/core?label=compose-preview-lab" alt="Maven Central"/>
</a>
<a href="https://central.sonatype.com/artifact/com.google.devtools.ksp/symbol-processing-api">
<img src="https://img.shields.io/maven-central/v/com.google.devtools.ksp/symbol-processing-api?label=ksp" alt="KSP Version"/>
</a>

```kts
plugins {
    // ⭐️ add ksp for collect `@Preview`
    id("com.google.devtools.ksp") version "<ksp-version>"
    // ⭐️ Add Compose Preview Lab Gradle plugin
    id("me.tbsten.compose.preview.lab") version "<compose-preview-lab-version>"
}

dependencies {
    implementation("me.tbsten.compose.preview.lab:core:<compose-preview-lab-version>")
    ksp("me.tbsten.compose.preview.lab:ksp-plugin:<compose-preview-lab-version>")
}
```

</details>

## Accelerating preview interactive mode

Use `PreviewLab` Composable and functions such as `***Field()` `onEvent()` to enhance Preview's
Interactive mode.

You can collect `@Preview`
and create an interactive Playground
like [Figma's Component Playground](https://help.figma.com/hc/en-us/articles/15023124644247-Guide-to-Dev-Mode#try-component-variations-in-the-component-playground).

```kt
@Preview
@Composable
private fun MyButtonPreview() = PreviewLab {
    MyButton(
        text = fieldValue { StringField("Click Me") },
        onClick = { onEvent("MyButton.onClick") },
    )
}
```

<img src="./docs/demo.gif" width="350" />

## Two core concepts

| Field                                                                                                                                                                                                                                                | Event                                                                                                                                              |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `fieldValue { ***Field(defaultValue) }` Allows you to manually change values in the Preview. <br> This allows you to say goodbye to the problem of PreviewParameterProvider displaying a large number of Previews and increasing the cognitive load. | When an event occurs in Preview (common examples: Button#onClick, HomeScreen#onIntent), call `onEvent()` to visualize the occurrence of the event. |
| TODO image                                                                                                                                                                                                                                           | TODO image                                                                                                                                         |

## Differences from [Storytale](https://github.com/Kotlin/Storytale)

A solution similar to Compose Preview Lab is [Storytale](https://github.com/Kotlin/Storytale) from
Jetbrains.
The table below shows the differences between the two.

(The following information is current as of 28.6.2025)

|                                          | Compose Preview Lab                                                                                                                                                                          | Storytale                                                                                                                                                                                                                                                                                                            |
|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Cataloging UI Component                  | ✅                                                                                                                                                                                            | ✅                                                                                                                                                                                                                                                                                                                    |
| View source code                         | ❌ <br> Future support is under consideration.                                                                                                                                                | ✅                                                                                                                                                                                                                                                                                                                    |
| Ease of preparing the Composable catalog | ✅ <br> Just enclose @Preview in `PreviewLab { }`.                                                                                                                                            | ⚠️ <br> You must have the code in the `***Stories` source set. Existing code with @Preview must be migrated.                                                                                                                                                                                                         |
| Parameter of your own type               | ✅ <br> By implementing a custom Field, you can freely customize the UI, including the operation UI. ([see](https://example.com)). It also provides useful utilities such as SelectableField. | ❌ <br> Not supported. Following the [source code](https://github.com/Kotlin/Storytale/blob/57f41aaee1a21d98d637fe752931715232deed9e/modules/gallery/src/commonMain/kotlin/org/jetbrains/compose/storytale/gallery/material3/StoryParameters.kt#L161) shows that there is a non-zero chance of support in the future. |

## Roadmap

- [x] Minimum preparation for Field and Event API
- [ ] Library stabilization (release of v1.0.0)
- [x] Fields that manipulate Compose classes
- [ ] Features that improve the UI review experience
- [ ] Display Source code
- [ ] Visual Regression Test by Compose Preview Lab
- [ ] Annotation function

## More information

- [WIP] [Documentation Site]()

### Practical Guides for Each Use Case

- [WIP] [Improve Review Experience on Pull Request by using Compose Preview Lab](https://github.com/TBSten/compose-preview-lab/blob/main/docs/improve-review-experience-on-pull-request.md)
- [WIP] [Improve documentation of component library](https://github.com/TBSten/compose-preview-lab/blob/main/docs/improve-documentation-of-component-library.md)

### Gain a Deeper Understanding of Compose Preview Lab

- [WIP] [Tips for Compose Preview Lab](https://github.com/TBSten/compose-preview-lab/blob/main/docs/tips.md)
- [WIP] [Customize and extend fields](https://github.com/TBSten/compose-preview-lab/blob/main/docs/customize-field.md)
- [WIP] [Design Documents](https://github.com/TBSten/compose-preview-lab/blob/main/docs/design/index.md)

### For those who contribute to development

- [Online Sample](https://tbsten.github.io/compose-preview-lab/integrationTest/main/js/)
- [WIP] [Contribution Guide](https://github.com/TBSten/compose-preview-lab/blob/main/docs/contribute-guide.md)
