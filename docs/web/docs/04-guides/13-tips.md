---
title: "[TODO] Tips"
sidebar_position: 13
---

:::warning

WIP

:::

import CompareCode from '@site/src/components/CompareCode';

# Compose Preview Lab の Tips

## 1. イベントを UiEvent sealed interface にまとめる

イベントが増えても、onEvent() を呼び出すラムダを追加する必要はありません。

<CompareCode
  language="kt"
  before={`
    fun HomeScreen(
      uiState: HomeUiState,
      onButtonClick: () -> Unit,
      onNameInput: (String) -> Unit,
    ) {
        ...
    }
  `}
  after={`
    fun HomeScreen(
      uiState: HomeUiState,
      onEvent: (HomeUiEvent) -> Unit,
    ) {
        ...
    }

    sealed interface HomeUiEvent {
      object ButtonClick : HomeUiEvent
      data class NameInput(val name: String) : HomeUiEvent
    }

    @Preview
    @Composable
    fun HomeScreenPreview() = PreviewLab {
      HomeScreen(
        ...
        // ⭐️ イベントが増えても、ここでコールバックの数を増やす必要はありません。
        onEvent = { onEvent("$it") },
      )
    }
  `}
/>

## 2. @ComposePreviewLabOption の displayName はプレースホルダーにできる

### 例

| @ComposePreviewLabOption の `displayName` | 表示例     |
|-------------------------------------------|----------------------------|
| `displayName = ""`                        | <img src="" width="300" /> | 
| `displayName = ""`                        | <img src="" width="300" /> | 
| `displayName = ""`                        | <img src="" width="300" /> | 

## 3. @ComposePreviewLabOption の displayName は `.` でグループ化できる

## 4. 独自のアプリテーマを定義している場合、ラベルとカスタムモディファイアを含むカラーモデルを準備する

アプリで独自の Theme（MaterialTheme ではない）を定義している場合、その Theme 内の各 color、textStyle などに `label` プロパティを含めてください。

```kt
data class MyTheme(
    // ⭐️ color の代わりに、label を設定した MyThemeColor を使用します。
    val primaryColor: MyThemeColor,
    // ...
)

data class MyThemeColor(
    // ⭐️ 単純に Color を使うのではなく、label プロパティを追加した MyThemeColor を準備します。
    val label: String,
    val color: Color,
)

val lightMyTheme = MyTheme(
    primary = MyThemeColor("primary", 0xFFFF0000),
    // ...
)
```

次に、プロジェクトに以下の Modifier を追加します。
この Modifier は背景を設定するだけでなく、`Modifier.labAnnotation()` を適用して、指定された色情報を表示するアノテーションを追加します。

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

最後に、以下のように呼び出します。

```kt
fun MyComponent() {
    Box(
        Modifier
            // ⭐️ 標準の background モディファイアの代わりに、カスタムモディファイアを使用します。
            .background(MyTheme.primaryColor)
    )
}
```

このようにモディファイアを管理することで、PreviewLab で指定された値の情報が[アノテーション機能](https://example.com)を使用して表示され、コードを一つずつ表示する必要がなくなります。

## 5. 開発ツール用のプレースホルダーを簡単に設定するためのソースセットを準備する

例えば、プロジェクトが Android と iOS アプリをターゲットにしているとします。
build.gradle.kts で `kotlin.androidTarget`、`kotlin.ios***` を有効にし、さらに Compose Preview Lab などの開発ツール用に `kotlin.jvm()`、`kotlin.js()`、`kotlin.wasmJs()` を追加します。

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

アプリケーションで機能を実装する際、実際のアプリケーションでは必要だが、開発ツールでは実装が困難または不便なため、プレースホルダーを表示したい場合があります。
この場合、以下のようなプレースホルダーを準備できます。

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

この場合、jvmMain、jsMain、wasmJsMain など、開発用に追加された各 sourceSet で Placeholder を呼び出すのは面倒です。
そこで、**devToolsMain** という sourceSet を作成し、そこでプレースホルダーを呼び出すようにします。これにより、Placeholder を複数回呼び出す必要がなくなります。

```
// before
commonMain
+-- Map.kt
+-- androidMain
+-- +-- Map.android.kt
+-- iosMain
+-- +-- Map.ios.kt
+-- jvmMain
+-- +-- Map.jvm.kt // 😕 各プラットフォームごとに Map.***.kt を準備する必要があり、非常に面倒です！
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
+-- +-- Map.devTools.kt // 😘 devTools 用に Map.***.kt を1つだけ準備すれば十分です！
+-- +-- jvmMain
+-- +-- jsMain
+-- +-- wasmJsMain
```

そのためには、build.gradle.kts に以下のコードを追加します。

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
