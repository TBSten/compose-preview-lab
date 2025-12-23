> [!WARNING]
> 🚨 This documentation is WIP.

# Tips for Compose Preview Lab

## 1. Putting together events into a UiEvent sealed interface

No need to add more lambdas that call onEvent() as the number of events grows.

```diff
 fun HomeScreen(
   uiState: HomeUiState,
-  onButtonClick: () -> Unit,
-  onNameInput: (String) -> Unit,
+  onEvent: (HomeUiEvent) -> Unit,
 ) {
     ...
 }

+sealed interface HomeUiEvent {
+  object ButtonClick : HomeUiEvent
+  data class NameInput(val name: String) : HomeUiEvent
+}

 @Preview
 @Composable
 fun HomeScreenPreview() = PreviewLab {
   HomeScreen(
     ...
     // ⭐️ Even if the number of events increases, there is no need to increase the number of callbacks here.
     onEvent = { onEvent("$it") },
   )
 }
```

## 2. The displayName of @ComposePreviewLabOption can be a placeholder

### Examples

| `displayName` of @ComposePreviewLabOption | Representation example     |
|-------------------------------------------|----------------------------|
| `displayName = ""`                        | <img src="" width="300" /> | 
| `displayName = ""`                        | <img src="" width="300" /> | 
| `displayName = ""`                        | <img src="" width="300" /> | 

## 3. The displayName of @ComposePreviewLabOption can be grouped by `.` for grouping

## 4. If you have defined your own app theme, prepare a color model that includes labels and custom modifiers.

If you have defined your own Theme (not MaterialTheme) in your app, include the `label`
property in each color, textStyle, etc. within that Theme.

```kt
data class MyTheme(
    // ⭐️Use MyThemeColor with label set instead of color.
    val primaryColor: MyThemeColor,
    // ...
)

data class MyThemeColor(
    // ⭐️ Instead of simply using Color, prepare MyThemeColor with the label property added.
    val label: String,
    val color: Color,
)

val lightMyTheme = MyTheme(
    primary = MyThemeColor("primary", 0xFFFF0000),
    // ...
)
```

Next, add the following Modifier to the project.
This Modifier not only sets the background, but also applies `Modifier.labAnnotation()` to add annotations that display the
specified color information.

```kt
fun Modifier.background(color: MyThemeColor, shape: Shape): Modifier =
    then(
        Modifier
            .background(color = color.color, shape = shape)
            .labAnnotation(
                label = color.label,
                description = "background(color = ${color.color}, shape = $shape)",
            )
    )
```

Finally, call these as follows.

```kt
fun MyComponent() {
    Box(
        Modifier
            // ⭐️ Use a custom modifier instead of the standard background modifier.
            .background(MyTheme.primaryColor)
    )
}
```

By managing modifiers in this way, the information for the specified values in PreviewLab will be displayed using
the [annotation feature](https://example.com),
eliminating the need to display the code one by one.

## 5. Prepare a set of sources to easily set up placeholders for development tools

For example, let's say your project targets Android and iOS apps.
You would enable `kotlin.androidTarget`, `kotlin.ios***` in build.gradle.kts, plus `kotlin.jvm()`, `kotlin.js()`,
` kotlin.wasmJs()` for devtools such as Compose Preview Lab.

<details>
    <summary>build.gradle.kts</summary>

```kts
plugins {
    kotlin("multiplatform")
}
kotlin {
    jvmToolchain(11)
    androidTarget {
        // https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // for devtools
    jvm()

    js {
        browser()
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }
}
```

</details>

As you implement features in your application, you may encounter cases where you want to display a placeholder because it is
difficult or inconvenient to implement in devtool, even though it is necessary in the actual application.
In this case, the following placeholders can be prepared.

```kt
@Composable
fun Placeholder(label: String) {
  Box(
    Modifier
      ...
  ) {
    Text("Placeholder: $label")
  }
}

// for simple function
fun placeholder(label: String) {
  println("placholder($label)")
}
```

```kt
// usage
// commonMain
@Composable
expect fun Map()

// androidMain, iosMain
actual fun Map() = /* TODO implementation */

// jvmMain, jsMain, wasmJsMain
actual fun Map() = Placeholder("Map()")
```

In this case, it is troublesome to call the Placeholder in each sourceSet added for development, such as jvmMain, jsMain, and
wasmJsMain.
Therefore, let's create a sourceSet called **devToolsMain** and call the placeholder there. This eliminates the need to call the
Placeholder multiple times.

```
// before
commonMain
+-- Map.kt
+-- androidMain
+-- +-- Map.android.kt
+-- iosMain
+-- +-- Map.ios.kt
+-- jvmMain
+-- +-- Map.jvm.kt // 😕 You need to prepare Map.***.kt for each platform, which is very cumbersome!
+-- jsMain
+-- +-- Map.js.kt
+-- wasmJsMain
+-- +-- Map.wasmJs.kt
```

```
// after
commonMain
+-- Map.kt
+-- androidMain
+-- +-- Map.android.kt
+-- iosMain
+-- +-- Map.ios.kt
+-- devTools
+-- +-- Map.devTools.kt // 😘 It is sufficient to prepare only one Map.***.kt for devTools!
+-- +-- jvmMain
+-- +-- jsMain
+-- +-- wasmJsMain
```

To do so, add the following code to build.gradle.kts.

```kts
kotlin {
  sourceSets {
    val devToolsMain by creating {
      dependsOn(commonMain)
      listOf(jvmMain, jsMain, wasmJsMain).forEach {
        it.get().dependsOn(devToolsMain)
      }
    }
  }
}
```





