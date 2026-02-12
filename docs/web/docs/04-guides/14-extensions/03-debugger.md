---
title: Debugger Extension
sidebar_position: 3
toc_max_heading_level: 4
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Debugger Extension

`extension-debugger` モジュールは、アプリケーションのデバッグ機能をランタイムで設定・操作できるデバッグメニューを提供します。API
コールの遅延、エラーのシミュレーション、カスタムログ機能などを実装できます。

## インストール

`starter` モジュールには含まれていないため、個別に依存関係を追加する必要があります。

<Tabs>
  <TabItem value="compose-multiplatform" label="Compose Multiplatform" default>

```kotlin title="build.gradle.kts"
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Compose Preview Lab
            implementation("me.tbsten.compose.preview.lab:starter:<version>")

            // highlight-start
            implementation("me.tbsten.compose.preview.lab:extension-debugger:<version>")
            // highlight-end
        }
    }
}
```

  </TabItem>
  <TabItem value="android" label="Android">

```kotlin title="app/build.gradle.kts"
dependencies {
    // Compose Preview Lab
    implementation("me.tbsten.compose.preview.lab:starter:<version>")

    // highlight-start
    // Debugger Extension
    implementation("me.tbsten.compose.preview.lab:extension-debugger:<version>")
    // highlight-end
}
```

  </TabItem>
</Tabs>

## 基本的な使い方

Compose Preview Lab の debugger は DebugMenu と DebugTool からなります。
DebugMenu は複数の DebugTool をまとめ、DebugTool は各種デバッグ機能を利用できる UI を提供します。

```
DebugMenu
├── DebugTool-1 （デバッグメニュー内の各タブに対応）
├── DebugTool-2
└── ...
```

import DebugMenuAndToolImage from "./debugger-debugmenu-and-tool.png"

<img src={DebugMenuAndToolImage} />

### 1. DebugMenu を定義する

アプリケーションで使用するデバッグメニューを DebugMenu() class を継承したクラスを使って定義します。

マルチモジュール構成の場合は :debug-menu モジュールなど専用のモジュールを用意し、デバッグ対象のモジュールで利用できるようにすると良いでしょう。

```kotlin
object AppDebugMenu : DebugMenu() {
}
```

DebugMenu は 複数の DebugTool で構成されます。それぞれの DebugTool がデバッグメニュー内のタブに相当します。
それぞれの DebugTool を tool を使って定義します。

```kotlin
object AppDebugMenu : DebugMenu() {
    // highlight-next-line
    val logger = tool { SimpleLogger() }
}
```

標準で用意されている DebugTool は [ビルトインの DebugTool](#ビルトインの-debugtool) のセクションを参照してください。
また独自の DebugTool を作成することもできます。（[カスタム DebugTool の作成](#カスタム-debugtool-の作成) を参照してください。）

### 2. DebugMenu UI を表示する

#### Dialog を使用（推奨）

`DebugMenu.Dialog()` をアプリの任意の場所で呼び出します。

```kotlin
@Composable
fun App() {
    // アプリケーションの UI
    MainContent()

    // highlight-start
    // デバッグメニューダイアログ（シェイクや Shift+D で表示）
    AppDebugMenu.Dialog()
    // highlight-end
}
```

`DebugMenu.Dialog()` はプラットフォーム固有のトリガーで自動的にデバッグメニューを表示します。

- **Android**, **iOS**: 端末のシェイク
- **JS/WasmJS**: キーボードショートカット（Shift+D）

import UiAndroidAndBrowserVideo from "./debugger-ui-android-and-browser.mp4"

<video src={UiAndroidAndBrowserVideo} controls />


トリガーを無効化して手動で制御することもできます：

```kotlin
@Composable
fun App() {
    val dialogState = rememberDebugMenuDialogState()

    Button(onClick = { 
        // highlight-start
        // デバッグメニューを表示する
        dialogState.show()
        // highlight-end
    }) {
        Text("Open Debug Menu")
    }

    // highlight-start
    // トリガーを無効化し、手動で表示を制御
    AppDebugMenu.Dialog(
        state = dialogState,
        trigger = DebugMenuTrigger.None,
    )
    // highlight-end
}
```

#### Desktop: 専用ウィンドウを使用

jvm platform では、専用のウィンドウを使用できます：

```kotlin
fun main() = application {
    val mainWindowState = rememberWindowState()

    // メインウィンドウ
    Window(
        onCloseRequest = ::exitApplication,
        state = mainWindowState,
    ) {
        MyApp()
    }

    // highlight-start
    // デバッグメニューウィンドウ（メインウィンドウに追従）
    DebugMenuWindow(
        debugMenu = AppDebugMenu,
        onCloseRequest = { /* handle close */ },
        baseWindowState = mainWindowState, // メインウィンドウの右側に配置
    )
    // highlight-end
}
```

import UiDesktopWindowImage from "./debugger-ui-desktop-window.png"

<img src={UiDesktopWindowImage} />

#### View を直接使用

Composable 関数内に直接 DebugMenu UI を表示できます。2つのレイアウトオプションから選択できます。

##### TabsView（タブレイアウト）

横スワイプ可能なタブで各ツールを切り替えます。1つのツールに集中したい場合に適しています。

```kotlin
@Composable
fun DebugScreen() {
    // highlight-next-line
    AppDebugMenu.TabsView()
}
```

import UiTabs from "./debugger-ui-tabs.png"

<img src={UiTabs} width="300" />

##### DropdownView（ドロップダウンレイアウト）

上部にドロップダウンセレクターを配置し、選択したツールのコンテンツを表示します。コンパクトな表示が必要な場合に適しています。

```kotlin
@Composable
fun DebugScreen() {
    // highlight-next-line
    AppDebugMenu.DropdownView()
}
```

import UiDropdown from "./debugger-ui-dropdown.png"

<img src={UiDropdown} width="300" />

## DebugTool

### ビルトインの DebugTool

#### SimpleLogger

logger として機能し、デバッグメニューに表示します。

```kotlin
object AppDebugMenu : DebugMenu() {
    val logger = tool {
        // highlight-next-line
        SimpleLogger()
    }
}

// any place
AppDebugMenu.logger.info("Updated")
AppDebugMenu.logger.warn("Invalid state", currentState)
AppDebugMenu.logger.error("Error", error = error)
```

import DebuggerSimpleLoggerImage from "./debugger-simple-logger.png"

<img src={DebuggerSimpleLoggerImage} width="300" />

#### basicFunctionDebugBehavior

任意の suspend 関数の挙動をデバッグメニューから変更できるようにします。これは UnitTest における Fake の高度な代替と考えることができます。

例えばデバッグメニューから アプリ内の UseCase や Repository のメソッドの挙動をデバッグメニューから変更することで、それを利用する ViewModel などの挙動をテストすることができるようになります。

```kotlin
object AppDebugMenu : DebugMenu() {
    val getItemListUseCaseBehavior = tool {
        // highlight-start
        basicFunctionDebugBehavior(
            label = "getItemListUseCaseBehavior",
            returnValueField = EnumField<GetItemListUseCaseDebugBehavior>(
                "Result",
                GetItemListUseCaseDebugBehaviorReturnValueType.Normal,
            ),
        )
        // highlight-end
    }
}

enum class GetItemListUseCaseDebugBehavior {
    EmptyFake,
    ManyListFake,
}
```

```kotlin
interface GetItemListUseCase {
    suspend fun execute(): List<Item>
}

class GetItemListUseCaseImpl : GetItemListUseCase {
    override suspend fun execute(): List<Item> { /* 実際の実装 */ }
}

class DebugGetItemListUseCase(
    private val default: GetItemListUseCase,
) : GetItemListUseCase {
    override suspend fun execute(): List<Item> = suspend { default.execute() }
        // highlight-start
        .debuggableBasic(AppDebugMenu.getItemListUseCaseBehavior) { behavior: GetItemListUseCaseDebugBehavior ->
            when(behavior) {
                EmptyFake -> emptyList()
                ManyListFake -> Item.manyListFake()
            }
        }.invoke()
        // highlight-end
}
```

import BasicfunctiondebugoptionImage from "./debugger-basicfunctiondebugoption.mp4"

<video
    src={BasicfunctiondebugoptionImage} 
    controls
/>

### カスタム DebugTool の作成

カスタムの DebugTool を用意することができます。

カスタム DebugTool の作成方法はシンプルで、DebugTool interface を実装し タブ名として表示される title, タブ選択時に表示されるデバッグメニュー上の UI の  Content メソッドを表示します。

```kotlin
// highlight-start
class MyLogger(
    override val title: String = "Logger",
// highlight-end
) : DebugTool {
    private var logs by mutableStateListOf<String>()

    @Composable
    // highlight-start
    override fun Content(context: DebugTool.ContentContext) {
        LogList(
            logList = logs,
        )
    }
    // highlight-end

    fun log(message: String) {
        logs.add(message)
    }
}

object AppDebugMenu : DebugMenu() {
    val logger = tool { MyLogger() }
}
```

DebugTool のメソッドを呼び出して内部状態を変更しデバッグメニューの状態を変更させることができます。

```kotlin
AppDebugMenu.logger.log("Hello !")
```

## 関連リンク

- [Fields Overview](../02-fields/01-overview.md) - Field の基本的な使い方
- [PolymorphicField](../02-fields/03-enhance-fields.md) - 複数の Field から選択する
- [CombinedField](../02-fields/03-enhance-fields.md) - 複数の Field を組み合わせる
