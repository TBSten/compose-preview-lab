# Supported Kotlin Versions

compose-preview-lab の compiler-plugin は ServiceLoader + 複数の compat module を shadow JAR に
バンドルすることで、1 つの published artifact で複数の Kotlin compiler バージョンに対応する。

## 現在のサポートマトリクス

| Kotlin                | サポート | 備考                                                                                       |
| --------------------- | -------- | ------------------------------------------------------------------------------------------ |
| 2.3.0 / 2.3.10        | ✅       | `IrDeclarationOriginCompat` (reflection で companion accessor を解決)                      |
| 2.3.20 / 2.3.21       | ✅       | 同上 + プロジェクト pin (`gradle/libs.versions.toml: kotlin = 2.3.21`)                     |
| 2.4.0-Beta2           | ✅       | `compat-k240_beta2` で `IrAnnotationImpl`、`IrAnnotationCompat` で `getAnnotation` を吸収  |
| 2.2.x 以前            | ❌       | サポート対象外 (Issue #149 のワークアラウンドだったのみ)                                   |

サポート対象の SSOT (CI / smoke-test スクリプトから参照される):
[`scripts/supported-kotlin-versions.txt`](../scripts/supported-kotlin-versions.txt)

## アーキテクチャ

```
compiler-plugin/                        # main (version-agnostic) — shadow JAR で publish
├── compat/                             # 共通 SPI: CompatContext, KotlinToolingVersion, ServiceLoader
compiler-plugin-compat-k230/            # Kotlin 2.3.x: FirFunction + IrConstructorCallImpl
compiler-plugin-compat-k240_beta2/      # Kotlin 2.4+: IrAnnotationImpl (annotations 型変更を吸収)
```

ビルド時に `compiler-plugin` の Shadow JAR が `compiler-plugin/compat` と各 `compiler-plugin-compat-*`
を取り込み、`META-INF/services/me.tbsten.compose.preview.lab.compiler.compat.CompatContext$Factory`
を merge する。

実行時:
1. `CompatContext.load()` が `ServiceLoader.load(CompatContext.Factory)` で全ファクトリを列挙
2. `META-INF/compiler.version` から現在の Kotlin compiler バージョンを検出
3. `currentVersion >= minVersion` を満たす factory のうち `minVersion` が最大のものを選択

## 新しい Kotlin バージョンを追加する手順

### A) Patch リリース (例: 2.3.30) で API drift がない場合

1. `scripts/supported-kotlin-versions.txt` に行を追加
2. `./scripts/smoke-test.sh 2.3.30` でローカル検証
3. PR 作成 (CI matrix が SSOT を読み込んで自動的に新バージョンを test する)

### B) Minor / Major リリース で API drift があるとき

1. `compiler-plugin-compat-kXYZ/` を新規作成
   - `version.txt` に `X.Y.Z` を記入
   - `build.gradle.kts` で `compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:X.Y.Z")` を pin
   - `src/main/kotlin/.../compat/kXYZ/CompatContextImpl.kt`:
     - 既存の最も近い compat module を delegate (`CompatContext by k230.CompatContextImpl()`) し、差分のみ override
   - `src/main/resources/META-INF/services/...$Factory` を登録
2. `settings.gradle.kts` に `include(":compiler-plugin-compat-kXYZ")` を追加
3. `compiler-plugin/build.gradle.kts` の `embedded` configuration に
   `add(embedded.name, projects.compilerPluginCompatKxyz)` を追加
4. `scripts/supported-kotlin-versions.txt` に新バージョンを追加
5. `./scripts/smoke-test.sh X.Y.Z` で検証
6. PR

### 参考実装 (clone 済み)

- Metro: `.local/tmp/metro/compiler-compat/` (`CompatContext` / `KotlinToolingVersion` / k* sub-modules)
- debuggable-compiler-plugin: `.local/tmp/debuggable-compiler-plugin/debuggable-compiler/compat/`
  (3-layer test strategy / smoke-test スクリプト)

## binary incompat を reflection で吸収しているもの

Kotlin 2.3 系の patch 間で binary 互換性が崩れる `IrDeclarationOrigin` の accessor 形式
(2.3.0/10 では `Companion.getX()`、2.3.20+ では static field GET) は、
[`IrDeclarationOriginCompat`](../compiler-plugin/compat/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/compat/IrDeclarationOriginCompat.kt)
が reflection で動的に解決している (`LOCAL_FUNCTION_FOR_LAMBDA`, `DELEGATE`)。
新たに別の `IrDeclarationOrigin` メンバを使うときは、ここに lookup を追加する。

同様に Kotlin 2.4 で戻り値型が変わった `IrUtilsKt.getAnnotation(...)` は
[`IrAnnotationCompat`](../compiler-plugin/compat/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/compat/IrAnnotationCompat.kt)
で `IrAnnotationContainer.annotations` を直接走査することで吸収している。
