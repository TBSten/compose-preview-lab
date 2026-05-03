# bug-017 PR2 — WasmJs / iOS integrationTest 拡張

PR1 (#163-#166) は kctfork JS_IR ユニットテストでマーカークラスベースの KLIB 集約パイプラインの **link 成功** までをカバーしている。本 PR2 (このブランチ) は実 Gradle build + KMP target で **runtime 値検証** を追加して、
ticket §テストケース §F (F-1〜F-5) を埋める。

## やること

1. `integrationTest/app/src/commonTest/kotlin/CrossModuleCollectPreviewsTest.kt` (本 PR で追加)
   - `appPreviews` (`collectAllModulePreviews()`) に `:uiLib` の preview が含まれることを assert
   - `commonTest` に置いたので JVM / Android / JS / WasmJS / iOS 全 target で同じテストが走る
2. CI workflow (`.github/workflows/...`) が `:integrationTest:wasmJsBrowserTest` と `:integrationTest:iosSimulatorArm64Test` を走らせるかを確認、足りなければ追加
3. Kotest tag `MultiplatformAggregation` を加えて部分実行可能にする (任意, ticket F-6)
4. JS で `kotlin.incremental.js=true` の状態で aggregation が動くことの smoke (ticket F-7 含む)

## 実行コマンド

```bash
(cd integrationTest && ./gradlew jvmTest jsBrowserTest wasmJsBrowserTest iosSimulatorArm64Test --continue)
```

iOS は macOS runner 必須。Linux runner では skip。

## 既存 jvmTest の扱い

`integrationTest/app/src/jvmTest/kotlin/CollectPreviewsCrossModuleTest.kt` は historical な「JVM でのみ動く」KDoc を持つ。本 PR2 で `commonTest` に同等のテストを追加した今、jvmTest 側は削除して OK。残しても assertion が重複するだけで害は無いが、メンテ対象が増える。
