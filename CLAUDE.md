# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Compose Preview Lab は、Compose の `@Preview` をインタラクティブな Component Playground に変換するライブラリ。Compose Multiplatform (Android, JVM, iOS, JS, WasmJS) をサポート。

主な機能:
- **Field API**: Preview 内でパラメータを動的に変更可能（`fieldValue { StringField("label", "default") }`）
- **Event API**: イベント発生を可視化（`onEvent("Button.onClick")`）
- **Gallery**: `@Preview` を収集してカタログ表示

## Build & Test Commands

```bash
# Lint
./gradlew ktlintCheck

# Binary compatibility check
./gradlew apiCheck

# Run all JVM tests (excludes PBT by default in CI)
./gradlew jvmTest

# Run specific test class
./gradlew jvmTest --tests "me.tbsten.compose.preview.lab.SomeTest"

# Run tests excluding Property-Based Tests
./gradlew jvmTest -Dkotest.tags='!PBT'

# Run only Property-Based Tests
./gradlew jvmTest -Dkotest.tags='PBT'

# Update API dump (after adding public APIs)
./gradlew apiDump

# Publish to Maven Local (for integration testing)
./gradlew publishToMavenLocal
```

### Integration Test

`integrationTest/` は独立した Gradle プロジェクト（composite build ではない）:

```bash
cd integrationTest
./gradlew jvmTest
```

### Dev Module

`dev` モジュールはライブラリの動作確認用。Hot Reload 対応:

```bash
./gradlew :dev:runHot
```

## Module Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         starter                              │
│              (全モジュールをバンドル、ユーザ向け)            │
└─────────────────────────────────────────────────────────────┘
         ↓
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│   gallery   │  │ preview-lab │  │    field    │
│ (カタログUI) │  │ (PreviewLab │  │ (Field実装) │
│             │  │  Composable) │  │             │
└─────────────┘  └─────────────┘  └─────────────┘
         ↓              ↓              ↓
┌─────────────┐  ┌─────────────┐
│     ui      │  │    core     │
│ (共通UI)    │  │ (型定義)    │
└─────────────┘  └─────────────┘
                       ↓
               ┌─────────────┐
               │  annotation │
               │ (@Preview等)│
               └─────────────┘

┌─────────────┐  ┌─────────────┐
│  ksp-plugin │  │gradle-plugin│
│(@Preview収集)│  │ (設定簡略化)│
└─────────────┘  └─────────────┘
```

- **core**: `PreviewLabPreview`, `PreviewLabField`, `PreviewLabEvent` などの基本型
- **field**: `StringField`, `IntField`, `BooleanField`, `SelectableField` などの Field 実装
- **preview-lab**: `PreviewLab` Composable（`fieldValue`, `fieldState`, `onEvent` を提供）
- **gallery**: `PreviewLabGallery` で Preview 一覧を表示
- **ui**: 内部 UI コンポーネント
- **ksp-plugin**: `@Preview` アノテーションを収集してコード生成
- **gradle-plugin**: KSP 設定を簡略化
- **extension/**: kotlinx-datetime, navigation などの拡張

## Build Logic (Convention Plugins)

`buildLogic/` に定義された Convention Plugin でボイラープレートを削減:

- **convention-jvm**: ksp-plugin, gradle-plugin 用
- **convention-kmp**: annotation, starter 用（KMP 設定）
- **convention-cmp**: core, field, gallery, preview-lab, ui, extension 用（CMP 設定）
- **convention-cmp-ui**: UI を持つ CMP モジュール用

各モジュールでは `moduleBaseName` を指定するだけで、androidNamespace, jsOutputModuleName, iosFrameworkBaseName が自動生成される。

## Testing

- テストフレームワーク: **Kotest**
- Property-Based Test は `PBT` タグ付き（CI では nightly 実行）
- Compose UI テストは `jvmTest` で実行可能

## Key Types

- `PreviewLabField<T>`: フィールドの基本インターフェース（`value`, `label`, `FieldView`）
- `MutablePreviewLabField<T>`: 変更可能なフィールド
- `PreviewLabScope`: `PreviewLab { }` 内で利用可能なスコープ（`fieldValue`, `fieldState`, `field`, `onEvent`）
- `CollectedPreview`: KSP で収集された Preview 情報
