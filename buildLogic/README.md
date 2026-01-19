# buildLogic

このディレクトリには、プロジェクト全体で共有される Convention Plugins が含まれています。

## Convention Plugins 一覧

### `convention-jvm`

Kotlin JVM プロジェクト用の基本設定を提供します。

**適用されるプラグイン:**
- `org.jetbrains.kotlin.jvm`

**設定内容:**
- `compilerOptions.optIn` に共通の opt-in アノテーションを追加

**使用例:**
```kotlin
plugins {
    alias(libs.plugins.conventionJvm)
}
```

**対象モジュール:** `ksp-plugin`, `gradle-plugin`

---

### `convention-kmp`

Kotlin Multiplatform プロジェクト用の基本設定を提供します。

**適用されるプラグイン:**
- `org.jetbrains.kotlin.multiplatform`
- `com.android.library`

**Extension:**
```kotlin
kmpConvention {
    moduleBaseName = "module-name"
}
```

**設定内容:**
- `jvmToolchain` (version catalog から取得)
- Android 設定 (`compileSdk`, `minSdk`)
- ターゲット: `androidTarget`, `jvm`, `js`, `wasmJs`, `iosX64`, `iosArm64`, `iosSimulatorArm64`
- `applyDefaultHierarchyTemplate()`
- `compilerOptions.optIn` に共通の opt-in アノテーションを追加
- `commonTest` に `kotlin("test")` を追加
- `moduleBaseName` から自動生成:
  - `android.namespace`
  - `js.outputModuleName` / `wasmJs.outputModuleName`
  - iOS framework `baseName`

**使用例:**
```kotlin
plugins {
    alias(libs.plugins.conventionKmp)
}

kmpConvention {
    moduleBaseName = "annotation"
}
```

**対象モジュール:** `annotation`, `starter`

---

### `convention-cmp`

Compose Multiplatform プロジェクト用の基本設定を提供します。
`convention-kmp` の設定に加えて、Compose 関連の設定を追加します。

**適用されるプラグイン:**
- `org.jetbrains.kotlin.multiplatform`
- `com.android.library`
- `org.jetbrains.kotlin.plugin.compose`
- `org.jetbrains.compose`

**Extension:**
```kotlin
cmpConvention {
    moduleBaseName = "module-name"
}
```

**設定内容:**
- `convention-kmp` の全設定
- `commonMain` に `composeRuntime` を追加
- `androidMain` に `composeUiTooling` を追加
- `jvmMain` に `compose.desktop.currentOs` を追加
- Android Test 依存関係 (`androidxUitestJunit4`, `androidxUitestTestManifest`)

**使用例:**
```kotlin
plugins {
    alias(libs.plugins.conventionCmp)
}

cmpConvention {
    moduleBaseName = "extension-kotlinx-datetime"
}
```

**対象モジュール:** `extension/kotlinx-datetime`, `extension/navigation`, `extension/navigation3`

---

### `convention-cmp-ui`

UI コンポーネントを含む Compose Multiplatform プロジェクト用の設定を提供します。
`convention-cmp` の設定に加えて、UI 関連の依存関係を追加します。

**適用されるプラグイン:**
- `convention-cmp` と同じ

**Extension:**
```kotlin
cmpConvention {
    moduleBaseName = "module-name"
}
```

**設定内容:**
- `convention-cmp` の全設定
- `commonMain` に以下を追加:
  - `composeFoundation`
  - `composeComponentsResources`
  - `composeUi`
  - `composeUiToolingPreview`

**使用例:**
```kotlin
plugins {
    alias(libs.plugins.conventionCmpUi)
}

cmpConvention {
    moduleBaseName = "core"
}
```

**対象モジュール:** `core`, `field`, `ui`, `gallery`, `preview-lab`

---

### `convention-format`

コードフォーマット (ktlint) の設定を提供します。

**適用されるプラグイン:**
- `org.jlleitschuh.gradle.ktlint`

**設定内容:**
- ktlint バージョンを version catalog から取得
- `generated/`, `resources/`, `buildkonfig/` ディレクトリを除外

**使用例:**
```kotlin
plugins {
    alias(libs.plugins.conventionFormat)
}
```

---

### `convention-publish`

Maven Central への公開設定を提供します。

**適用されるプラグイン:**
- `com.vanniktech.maven.publish`

**Extension:**
```kotlin
publishConvention {
    artifactName = "Core"
    artifactId = "core"
    description = "Description of the artifact"
}
```

**設定内容:**
- Maven Central への公開設定
- 署名設定 (publishToMavenLocal 以外)
- POM 情報 (ライセンス、開発者、SCM)
- Dokka ドキュメント生成

**使用例:**
```kotlin
plugins {
    alias(libs.plugins.conventionPublish)
}

publishConvention {
    artifactName = "Core"
    artifactId = "core"
    description = "Core module description"
}
```

---

## モジュール別プラグイン対応表

| モジュール | conventionJvm | conventionKmp | conventionCmp | conventionCmpUi | conventionFormat | conventionPublish |
|-----------|:-------------:|:-------------:|:-------------:|:---------------:|:----------------:|:-----------------:|
| annotation | | ✓ | | | ✓ | ✓ |
| core | | | | ✓ | ✓ | ✓ |
| field | | | | ✓ | ✓ | ✓ |
| ui | | | | ✓ | ✓ | ✓ |
| gallery | | | | ✓ | ✓ | ✓ |
| preview-lab | | | | ✓ | ✓ | ✓ |
| starter | | ✓ | | | ✓ | ✓ |
| ksp-plugin | ✓ | | | | ✓ | ✓ |
| gradle-plugin | ✓ | | | | ✓ | ✓ |
| extension/kotlinx-datetime | | | ✓ | | ✓ | ✓ |
| extension/navigation | | | ✓ | | ✓ | ✓ |
| extension/navigation3 | | | ✓ | | ✓ | ✓ |

---

## Version Catalog 定数

以下の定数が `gradle/libs.versions.toml` で定義されており、Convention Plugins から参照されています:

| 定数名 | 値 | 用途 |
|--------|-----|------|
| `jvmToolchain` | `17` | Kotlin JVM toolchain バージョン |
| `androidCompileSdk` | `36` | Android compileSdk |
| `androidMinSdk` | `23` | Android minSdk |
| `jsTarget` | `es2015` | JS/WasmJS コンパイラターゲット |

---

## ファイル構成

```
buildLogic/
├── build.gradle.kts          # buildLogic モジュールの設定
├── README.md                  # このファイル
└── src/main/kotlin/
    ├── CmpConventionExtension.kt
    ├── CmpConventionPlugin.kt
    ├── CmpUiConventionPlugin.kt
    ├── ConfigureCmp.kt           # CMP 共通設定
    ├── ConfigureKmp.kt           # KMP 共通設定
    ├── FormatConventionPlugin.kt
    ├── JvmConventionPlugin.kt
    ├── KmpConventionExtension.kt
    ├── KmpConventionPlugin.kt
    ├── PublishConventionPlugin.kt
    └── util/
        ├── Android.kt            # Android/Kotlin 拡張関数、COMMON_OPT_INS
        ├── Dokka.kt              # Dokka 設定
        ├── Gradle.kt             # Version Catalog アクセサ
        ├── Ktlint.kt             # Ktlint 設定
        ├── ModuleNameUtils.kt    # モジュール名変換ユーティリティ
        └── Publish.kt            # Maven Publish 設定
```
