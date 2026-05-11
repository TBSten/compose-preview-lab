---
paths:
    - "compiler-plugin/**/*.kt"
---

# Rule: compiler-plugin 関数の KDoc には生成/変換される Kotlin コード例を必ず含める

## 適用範囲

`compiler-plugin/` 配下の **IR / FIR を操作する関数** すべて。具体的には:

- `compiler-plugin/src/main/kotlin/.../ir/` — `IrElementTransformer*`, `IrGenerationExtension`, IR builder / generator
- `compiler-plugin/src/main/kotlin/.../fir/` — `FirDeclarationGenerationExtension`, `FirStatusTransformerExtension`, FIR builder
- 上記から呼び出される helper / builder (例: `PreviewListIrBuilder`, `PreviewExportHintGenerator`, `CollectedPreviewIrBuilder`)

純粋なリフレクションラッパーや一行で済む private helper は対象外。

## 要件

基本的に英語で書く。

これらの関数の KDoc には、**何が入って・何が出る/書き換わるか** を Kotlin
ソース相当の擬似コードで示すこと。最低限、以下のいずれかのパターンを使う:

- **Before → After** (IR transformer / IR rewriter)
- **Input → Generated** (synthetic declaration generator)
- **Sample call → Resulting IR** (builder / IR factory)

擬似コードは「semantically equivalent な Kotlin ソース」で十分 (`@Composable lambda` を素朴な lambda で書く、など)。**バイトコードや
IR dump をそのまま貼る必要はない** が、読み手が compile 結果を頭で再現できる粒度であること。

### 利用箇所の列挙はしない

KDoc に「`Used by:` / `使用箇所:` / `Callers:`」のような **呼び出し元の列挙** は書かないこと。 列挙は実装が増えるたびに drift し、 メンテコストが高い。 必要に応じて以下のように **使い方の例** だけ示すこと:

- `Sample call → Resulting IR` 形式の usage example を 1〜2 個示す
- `[他クラス参照]` リンクで呼び出し関係をたどれるようにする
- 「いつ呼ばれるか」のセマンティクスは散文で短く書く (`pluginContext.referenceFunctions(...)` で発見される時、 など)

呼び出しグラフは IDE の Find Usages が真の source of truth。 KDoc 側に二重管理しない。

## 例

### Good — 生成系

````kotlin
/**
 * 同 IR module に synthetic IrFile を 1 つ追加し、その中に hint 関数を 1 個生成する。
 *
 * `propertyFqn = "uiLib.uiLibPreviews"` のとき、生成される Kotlin (semantically) は:
 *
 * ```kotlin
 * // me.tbsten.compose.preview.lab.exports/PreviewLabExport_uiLib_uiLibPreviews.kt
 * package me.tbsten.compose.preview.lab.exports
 *
 * @PreviewExportHint(fqn = "uiLib.uiLibPreviews")
 * public fun previewLabExport(value: PreviewExport): Unit {}
 * ```
 *
 * file metadata は `FirMetadataSource.File` で配線するため、`kotlin.Metadata(k=2)`
 * (file facade) として書き出され、downstream module の `referenceFunctions` で
 * top-level callable として発見できる。
 */
fun generateHint(propertyFqn: String, sourceFile: IrFile)
````

### Good — 変換系

````kotlin
/**
 * `val x by collectModulePreviews()` プロパティの delegate field initializer を書き換える。
 *
 * **Before**:
 * ```kotlin
 * val appPreviews by collectModulePreviews()
 * // delegate field initializer = collectModulePreviews() の sentinel call
 * ```
 *
 * **After** (semantically equivalent):
 * ```kotlin
 * val appPreviews by PreviewExport(
 *     lazy { listOf(CollectedPreview("id1", "name1", ...), ...) }
 * )
 * ```
 *
 * `collectAllModulePreviews()` の場合は、自モジュールの list に依存モジュールの
 * preview を `+` で連結した上で `PreviewExport(lazy { ... })` でラップする。
 */
fun replaceCollectPreviewsProperty(property: IrProperty)
````

### Good — builder

````kotlin
/**
 * `CollectedPreview(...)` constructor 呼び出しの IR を構築する。
 *
 * **入力**: `PreviewFunctionInfo(id="id1", displayName="MyButton", function=fun MyButton())`
 *
 * **出力 IR は以下と等価**:
 * ```kotlin
 * CollectedPreview(
 *     id = "id1",
 *     displayName = "MyButton",
 *     filePath = "src/.../MyButton.kt",
 *     content = @Composable { MyButton() },
 * )
 * ```
 */
fun buildCollectedPreviewExpr(preview: PreviewFunctionInfo, ...): IrExpression
````

### Bad — 抽象的すぎる

```kotlin
/**
 * Replaces the property's delegate.    ← 何が入って何が出るか不明
 * Generates a hint function.           ← どんな関数？
 * Builds the IR for a constructor call.← 何のクラスの？引数は？
 */
fun replaceCollectPreviewsProperty(property: IrProperty)
```

### Bad — IR dump をそのまま貼る

```kotlin
/**
 * Generates:
 * FUN name:previewLabExport visibility:public modality:FINAL returnType:kotlin.Unit
 *   VALUE_PARAMETER kind:Regular name:value index:0 type:PreviewExport
 *   ...
 */
```

→ 読み手は IR を頭で Kotlin に戻す必要があり、認知負荷が高い。**Kotlin ソースで書く**。

### Bad — 利用箇所を列挙

```kotlin
/**
 * Hint stub の origin として使われる key。
 *
 * Used by:
 * - PreviewHintFirGenerator.generateFunctions
 * - FillPreviewHintIrBody.fillHintBody
 * - ReplaceCollectPreviewsFunBody.replaceCollectPreviewsProperty
 */
object PreviewLabHint : GeneratedDeclarationKey()
```

→ 呼び出し元はリファクタや機能追加で増減する。 KDoc 側で列挙すると簡単に古くなる。 代わりに 1 行の semantics と link で十分:

```kotlin
/**
 * Hint stub の origin として使われる key。 IR 側 ([FillPreviewHintIrBody]) は
 * この key で hint 関数を識別して body を埋める。
 */
object PreviewLabHint : GeneratedDeclarationKey()
```

## チェックリスト (PR レビュー時)

- [ ] `compiler-plugin/.../ir/` 配下に新規追加された public/internal 関数すべてに KDoc がある
- [ ] KDoc に Before/After (or Input/Generated) の **Kotlin 擬似コードブロック** がある
- [ ] 例で使われている FQN や型名は実装と一致している
- [ ] IR dump やバイトコードをそのまま貼っていない (Kotlin ソースで書かれている)
- [ ] `Used by:` / `使用箇所:` / `Callers:` のような呼び出し元 **列挙** がない (IDE の Find Usages に任せる)
