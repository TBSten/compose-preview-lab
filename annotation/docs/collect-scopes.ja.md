# Collection Scopes (日本語)

> [English](./collect-scopes.md)

[`@ComposePreviewLabOption`](../src/commonMain/kotlin/me/tbsten/compose/preview/lab/ComposePreviewLabOption.kt)
の **`collectScopes`** 機能と、 companion の `DefaultCollectScope` sentinel に関する詳細ドキュメント。
annotation の KDoc には 1 段落の要約のみを残し、 設計意図 / ユースケース / ABI セマンティクスはこのファイルが
single source of truth。

scope 機能全体は現状 `@ExperimentalComposePreviewLabApi` gate。 no-arg の
`collectModulePreviews()` / `collectAllModulePreviews()` は stable のまま使えるが、
`scope: String` overload と `collectScopes` 引数の利用には `@OptIn` が必要。

## なぜ `collectScopes` が存在するか

`collectScopes` は主に **component library の preview が consumer application の gallery に
混ざらないようにする** ためにある。 ほとんどのプロジェクトは 1 module = 1 scope を一度決めれば、
`@Preview` ごとに override することは無い。 multi-element 形は対応するが、 「複数 bucket に
正当に属する preview」 という少数派の用途。

## Primary use case — library / app 隔離 (1 module : 1 scope)

component library の preview は library maintainer が iterate するために publish される。
library の consumer (app) はそれらが自分の `collectAllModulePreviews()` に出てきて欲しくない。
慣例パターン: library 側 Gradle DSL に 1 行書くだけで、 その module の全 `@Preview` が
自動的に library scope に登録される (`@Preview` ごとの annotation 不要)。

```kotlin
// -- :ui-library/build.gradle.kts --
composePreviewLab {
    collectPreviews {
        defaultCollectScope = "acme_ui"
    }
}

// -- :ui-library/Button.kt --
@Preview @Composable
fun PrimaryButtonPreview() { PrimaryButton() }
// ↑ DSL のおかげで previewHint_acme_ui に出る。

// -- :ui-library/Previews.kt --
@file:OptIn(me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi::class)
val libraryGallery by collectAllModulePreviews(scope = "acme_ui")

// -- :app (:ui-library に依存) --
@Preview @Composable fun LoginScreenPreview() { LoginScreen() }   // → previewHint_default
val appGallery by collectAllModulePreviews()                      // LoginScreenPreview のみ。
                                                                  // PrimaryButtonPreview は acme_ui に
                                                                  // 隠れて見えない。
```

仕組み: compiler plugin が Gradle DSL の
`composePreviewLab.collectPreviews.defaultCollectScope` を読み、 `@Preview` ごとの値が
`[DefaultCollectScope]` (= `["default"]`) のときにそれで置換する。 結果として
`@ComposePreviewLabOption.collectScopes` を default のままにしても、 module 単位で隔離できる。

no-arg の `collectAllModulePreviews()` は stable のまま使え、 `@OptIn` 不要。 experimental
扱いは `scope: String` overload と `collectScopes` 引数のみ。

## Secondary use case — 1 preview を複数 scope に登録

1 つの preview が複数 bucket に属することが正当な場合 (例: library 自身の gallery と
"showcase" gallery で共有)、 `@Preview` ごとの annotation 形を使って scope を明示列挙する。

```kotlin
@file:OptIn(me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi::class)

@ComposePreviewLabOption(collectScopes = ["acme_ui", "showcase"])
@Preview @Composable
fun ButtonShowcase() { PrimaryButton() }

val libraryGallery by collectAllModulePreviews(scope = "acme_ui")   // 見える
val showcaseGallery by collectAllModulePreviews(scope = "showcase") // こちらも見える
```

各 entry は `[A-Za-z0-9_]+` に match する必要がある (compiler plugin が synthetic hint 関数名
`previewHint_<scope>` に埋め込むため)。 FIR の `CollectScopeAnnotationChecker` が
analysis 時 (= IR generation の前) に element 単位で invalid 値を報告するので、
IDE のハイライタにエラーが出る。 `ignore = true` を指定すると scope 列挙の数に関係なく
常に勝つ (= preview を集めない)。

## compile 時の resolution 順序

各 `@Preview` と各 `collect[All]ModulePreviews` 呼び出しごとに:

1. annotation 引数 / call-site 引数が `[DefaultCollectScope]` / `DefaultCollectScope` 以外なら
   それがそのまま採用される。
2. それ以外なら、 compiler plugin が module の
   `composePreviewLab.collectPreviews.defaultCollectScope` (Gradle DSL で設定) で置換する。
   library が「全 preview を library 専用 bucket に固定」 を `@Preview` ごとの annotation
   なしで実現できるのはここ。
3. Gradle DSL も未設定なら runtime default `"default"` が適用される。

## experimental marker visibility — BCV の癖

`collectScopes` プロパティは `@property:ExperimentalComposePreviewLabApi` でマークしてあり、
use-site で opt-in が要求される。 ただし BCV の `nonPublicMarkers` filter はこのプロパティを
**KLIB** baseline では suppress するが、 **JVM** / **Android** baseline では abstract getter
を記録し続ける。 これは Kotlin が `@property:` marker を getter ではなく synthetic な
`<name>$annotations()` helper に付与するためで、 BCV の method-level annotation scan が
それを拾えないため。 `@get:` / `@field:` / `@param:` の use-site qualifier は
`@RequiresOptIn` marker には Kotlin の opt-in 機構が禁止しているので、 Kotlin source 側だけ
からは asymmetry を解消できない。 `collectScopes` の experimental→stable promotion は
**release-notes / KDoc の課題** として扱い、 baseline diff からのシグナルとしては扱わない。

### companion と const の両方に experimental marker が要る理由

`Companion` object と `DefaultCollectScope` のそれぞれが
`@ExperimentalComposePreviewLabApi` を持つのは **相補的な理由** による:

- companion 単位の marker は `Companion` *クラス自体* を全 BCV baseline (KLIB/JVM/Android)
  から外す。 これがないと、 const をいつか別の場所に動かしたあとも、 empty companion class の
  signature が `*.api` / `*.klib.api` baseline に永久に残り、 stable member が無いのに
  `Companion` class が ABI surface として lock-in される。
- const 単位の marker は const *member 自身* を filter する。 KLIB 上では companion 単位
  filter と nest し、 JVM / Android 上では const が外側の `ComposePreviewLabOption` クラスに
  `public static final field` として平坦化される (= companion 単位 annotation はその
  平坦化された field に伝播しない、 詳細は root build.gradle.kts の Known Limitation note 参照)。
  この const 単位 marker がないと、 field が JVM / Android baseline に永久に残る。
