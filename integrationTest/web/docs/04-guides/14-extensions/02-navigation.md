---
title: Navigation Extension
sidebar_position: 2
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import EmbeddedPreviewLab from '@site/src/components/EmbeddedPreviewLab';

# Navigation Extension

`extension-navigation` モジュールは、[Compose Navigation](https://developer.android.com/develop/ui/compose/navigation) の `NavHostController` を Inspector から操作・表示するための Field を提供します。

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

            // highlight-next-line
            // Navigation Extension
            // highlight-next-line
            implementation("me.tbsten.compose.preview.lab:extension-navigation:<version>")

            // Navigation 本体も必要
            implementation("org.jetbrains.androidx.navigation:navigation-compose:<navigation-version>")
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

    // highlight-next-line
    // Navigation Extension
    // highlight-next-line
    implementation("me.tbsten.compose.preview.lab:extension-navigation:<version>")

    // Navigation 本体も必要
    implementation("androidx.navigation:navigation-compose:<navigation-version>")
}
```

  </TabItem>
</Tabs>

## 利用可能な Field

| Field | 説明 |
|-------|------|
| [`NavControllerField`](#navcontrollerfield) | NavHostController の状態表示・操作 |

## 使用例

### NavControllerField

`NavHostController` を Inspector から操作できる Field です。以下の機能を提供します：

- **BackStack 表示**: 現在のバックスタック履歴を表示
- **Pop Back**: バックスタックから戻る操作
- **Route 選択**: `PolymorphicField` を使用して遷移先を選択
- **パラメータ編集**: 引数付き route のパラメータを動的に編集可能

```kotlin
@Serializable
object Home

@Serializable
data class Profile(val userId: String)

@Serializable
object Settings

@Preview
@Composable
fun MyScreenPreview() = PreviewLab {
    val _navController = rememberNavController()
    // highlight-start
    val navController = fieldValue("navController") {
        NavControllerField(
            label = "navController",
            navController = _navController,
            routes = listOf(
                // 引数なしの route は FixedField を使用
                FixedField("Home", Home),
                // 引数付きの route は CombinedField1 等を使用
                CombinedField1(
                    label = "Profile",
                    field1 = StringField("userId", "default"),
                    combine = { userId -> Profile(userId = userId) },
                    split = { profile -> splitedOf(profile.userId) },
                ),
                FixedField("Settings", Settings),
            ),
        )
    }
    // highlight-end

    NavHost(
        navController = navController,
        startDestination = Home,
    ) {
        composable<Home> { HomeScreen() }
        composable<Profile> { ProfileScreen() }
        composable<Settings> { SettingsScreen() }
    }
}
```

<EmbeddedPreviewLab
 previewId="NavControllerFieldExample"
 title="NavControllerField Example"
/>

### パラメータ

| パラメータ | 型 | 説明 |
|-----------|-----|------|
| `label` | `String` | Field のラベル |
| `navController` | `NavHostController` | 操作対象の NavHostController |
| `routes` | `List<PreviewLabField<out Any>>` | 遷移先の route Field リスト |

### routes の定義

`routes` パラメータには、各遷移先を表す Field のリストを渡します。

#### 引数なしの route

`FixedField` を使用します：

```kotlin
FixedField("Home", Home)
FixedField("Settings", Settings)
```

#### 引数ありの route

`combined()` 関数を使用します：

```kotlin
// 1引数の route
combined(
    label = "Profile",
    field = StringField("userId", "default"),
    combine = { userId -> Profile(userId = userId) },
    split = { profile -> profile.userId },
)

// 2引数の route
combined(
    label = "Article",
    field1 = IntField("articleId", 1),
    field2 = BooleanField("showComments", true),
    combine = { articleId, showComments -> Article(articleId, showComments) },
    split = { article -> splitedOf(article.articleId, article.showComments) },
)
```

## 型安全なルート定義

Navigation 2.8 以降では、`@Serializable` アノテーションを使用した型安全なルート定義が推奨されています：

```kotlin
@Serializable
object Home

@Serializable
data class Profile(val userId: String)

@Serializable
data class Article(val articleId: Int, val showComments: Boolean = true)
```

これらの型を `routes` パラメータで使用することで、型安全にナビゲーション操作ができます。

## 関連リンク

- [Fields Overview](../02-fields/01-overview.md) - Field の基本的な使い方
- [PolymorphicField](../02-fields/01-overview.md) - 複数の Field から選択する
- [CombinedField](../02-fields/01-overview.md) - 複数の Field を組み合わせる
- [Navigation in Compose](https://developer.android.com/develop/ui/compose/navigation) - Compose Navigation 公式ドキュメント
- [Type-safe Navigation](https://developer.android.com/guide/navigation/design/type-safety) - 型安全なナビゲーション
