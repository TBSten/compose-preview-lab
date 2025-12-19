---
title: "[TODO] Compose Preview Lab を使った debug menu の構築"
sidebar_position: 7
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Compose Preview Lab を使った debug menu の構築

このページでは、Compose Preview Lab を使って **デバッグメニュー（debug menu）** を構築する方法を紹介します。  
デバッグメニューは、開発中やテスト時にアプリの内部状態を確認・操作するための画面で、Compose Preview Lab の Field と Events 機能を活用することで、簡単に構築できます。

## ゴール

- PreviewLab を使ってデバッグメニュー画面を構築する  
- Field を使ってアプリの設定や状態を動的に変更できるようにする  
- Events を使ってデバッグ操作のログを記録する  
- 本番ビルドでは無効化できるようにする

## 1. デバッグメニューの基本構造

デバッグメニューは、アプリの設定や状態を確認・変更するための画面です。  
`PreviewLab` を使って、以下のような構造で実装できます。

```kotlin title="DebugMenu.kt"
@Composable
fun DebugMenu(
    onDismiss: () -> Unit,
) = PreviewLab {
    val apiEndpoint by fieldState {
        StringField("API Endpoint", "https://api.example.com")
    }
    val enableLogging by fieldState {
        BooleanField("Enable Logging", false)
    }
    val logLevel by fieldState {
        EnumField("Log Level", LogLevel.INFO)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Debug Menu",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // API Endpoint の設定
        OutlinedTextField(
            value = apiEndpoint,
            onValueChange = { apiEndpoint = it },
            label = { Text("API Endpoint") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Logging の有効/無効
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = enableLogging,
                onCheckedChange = { enableLogging = it },
            )
            Text("Enable Logging")
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Log Level の選択
        Text("Log Level")
        Row {
            LogLevel.values().forEach { level ->
                FilterChip(
                    selected = logLevel == level,
                    onClick = { logLevel = level },
                    label = { Text(level.name) },
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // アクションボタン
        Button(
            onClick = {
                onEvent("DebugMenu.ApplySettings", mapOf(
                    "apiEndpoint" to apiEndpoint,
                    "enableLogging" to enableLogging,
                    "logLevel" to logLevel.name,
                ))
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Apply Settings")
        }
    }
}
```

<EmbeddedPreviewLab
  previewId="FieldQuickSummary"
/>

## 2. PreviewLab を使ったデバッグメニューの利点

通常のデバッグメニューと異なり、PreviewLab を使うことで以下のメリットがあります：

- **Field による動的な値の変更**: コードを変更せずに、実行時に設定値を変更できる  
- **Events による操作ログ**: どの設定が変更されたかを記録できる  
- **Web 上でも動作**: JS/WasmJS ターゲットでビルドすれば、ブラウザ上でもデバッグメニューを確認できる  
- **UI カタログに統合**: PreviewLabGallery に含めることで、他の Preview と一緒に管理できる

## 3. デバッグメニューをアプリに統合する

デバッグメニューをアプリに統合する方法は、プラットフォームによって異なります。

### Android / Desktop の場合

```kotlin title="MainActivity.kt / main.kt"
@Composable
fun App() {
    var showDebugMenu by remember { mutableStateOf(false) }

    // デバッグビルド時のみデバッグメニューを表示
    if (BuildConfig.DEBUG && showDebugMenu) {
        DebugMenu(
            onDismiss = { showDebugMenu = false },
        )
    } else {
        // 通常のアプリ画面
        MainScreen(
            onOpenDebugMenu = { showDebugMenu = true },
        )
    }
}
```

### Web の場合

```kotlin title="main.kt"
fun main() = previewLabApplication(
    previewList = app.PreviewList,
) {
    var showDebugMenu by remember { mutableStateOf(false) }

    if (showDebugMenu) {
        DebugMenu(
            onDismiss = { showDebugMenu = false },
        )
    } else {
        MainScreen(
            onOpenDebugMenu = { showDebugMenu = true },
        )
    }
}
```

:::tip デバッグメニューの表示方法
- **シェイクジェスチャー**: Android では端末を振ることでデバッグメニューを開く  
- **設定画面から**: 設定画面に「開発者向けオプション」を追加  
- **キーボードショートカット**: Desktop では特定のキー組み合わせで開く  
- **URL パラメータ**: Web では `?debug=true` のようなパラメータで開く
:::

## 4. デバッグメニューを PreviewLabGallery に含める

デバッグメニューも Preview として定義することで、PreviewLabGallery に含めることができます。

```kotlin title="DebugMenuPreview.kt"
@Preview
@Composable
fun DebugMenuPreview() = PreviewLab {
    DebugMenu(
        onDismiss = { onEvent("DebugMenu.Dismiss") },
    )
}
```

これにより、UI カタログからデバッグメニューを確認できるようになります。

## 5. 高度な使い方

### 複数のデバッグ設定をグループ化する

```kotlin
@Composable
fun DebugMenu() = PreviewLab {
    val networkSettings by fieldState {
        GroupField("Network Settings", listOf(
            StringField("API Endpoint", "https://api.example.com"),
            IntField("Timeout (ms)", 5000),
        ))
    }
    val featureFlags by fieldState {
        GroupField("Feature Flags", listOf(
            BooleanField("Enable Feature A", false),
            BooleanField("Enable Feature B", true),
        ))
    }

    // ...
}
```

### デバッグ操作の履歴を表示する

```kotlin
@Composable
fun DebugMenu() = PreviewLab {
    val eventHistory = remember { mutableStateListOf<String>() }

    // Events を記録
    LaunchedEffect(Unit) {
        // Events の変更を監視して履歴に追加
    }

    Column {
        // デバッグ設定...
        
        Text("Event History")
        eventHistory.forEach { event ->
            Text(event)
        }
    }
}
```

## 6. ベストプラクティス

:::tip デバッグメニュー構築時のチェックリスト
- **本番ビルドでは無効化**: `BuildConfig.DEBUG` や条件付きコンパイルで本番ビルドから除外する  
- **設定の永続化**: 変更した設定を SharedPreferences や LocalStorage に保存する  
- **リセット機能**: デフォルト値に戻すボタンを用意する  
- **危険な操作には警告**: データ削除などの操作には確認ダイアログを表示する  
- **操作ログの記録**: Events を使って、どの設定が変更されたかを記録する  
:::

## 7. 次のステップ

- [Preview を収集して UI カタログを構築する](./preview-ui-catalog) で UI カタログの構築方法を学ぶ  
- [All Fields](../guides/fields/all-fields) で利用可能な Field の一覧を確認する  
- [Events](../guides/events) で Events 機能の詳細を学ぶ  
- [Inspector Tab](../guides/inspector-tab) でカスタムタブを追加し、デバッグ情報を表示する

