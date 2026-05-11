# 探索カテゴリ一覧

各カテゴリで 8〜10 分を目安に探索する。 sequential に cat1 → cat5 を回す。

## cat1: 静的解析

- 推奨ツール: `Read` / `Glob` / `Grep`
- チェック観点:
  - **KDoc / コメントの drift**: 関数シグネチャと KDoc の不一致、 「used by X」のような壊れやすい記述
  - **Single source of truth**: 同じ定数・設定が複数箇所で定義されていないか（CLAUDE.md でも明文化）
  - **Silent failure**: try-catch の中で warn なく握り潰している箇所
  - **型設計**: nullable 過多・widening type・enum と sealed の混在
  - **TODO grep**: `grep -rn 'TODO' --include='*.kt'` で残骸を確認、 古いものを抽出
- 注意:
  - `compiler-plugin/` 配下は `.claude/rules/compiler-plugin-kdoc.md` のチェックも兼ねる
  - core / field / preview-lab の公開 API は特に丁寧に

## cat2: CI ログ・apiDump baseline・docs 整合

- 推奨ツール: `Bash` (gh / gradle), `Read`, `Grep`
- チェック観点:
  - **nightly 過去 run の warning grep**: `gh run list --workflow nightly-checking.yml --limit 5`
  - **apiDump baseline drift**: `./gradlew apiCheck --continue` を実行し、 失敗があれば起票
  - **docs / web の link 切れ**: docs 配下の `*.md` から URL を抽出し、 一部 sampling で `WebFetch` で生存確認
  - **README と CLAUDE.md の差分**: 古い記述が残っていないか
- 注意:
  - gradle コマンドは `timeout 8m ./gradlew ... > .local/nightly-exploration/tmp/<ts>-apiCheck.log 2>&1` のように完全保存
  - 80% 以上の warning は P2 / P3 として軽く起票してよい

## cat3: 動的ビルド・テスト

- 推奨ツール: `Bash`
- チェック観点:
  - **ライブラリ本体の jvmTest**: `./gradlew jvmTest -Dkotest.tags='!PBT' --continue`
  - **integrationTest の jvmTest**: `(cd integrationTest && ./gradlew jvmTest -Dkotest.tags='!PBT' --continue)`
  - **docs の jvmTest**: `(cd docs && ./gradlew jvmTest -Dkotest.tags='!PBT' --continue)`
  - **publishToMavenLocal**: artifact が正常に出るか
- 注意:
  - PBT は別 job が回しているので **PBT タグは除外** （`-Dkotest.tags='!PBT'`）
  - テスト失敗があれば原因切り分けを 1 回試み、 切り分けが付かない場合はそのまま P1 として起票
  - ログは `.local/nightly-exploration/tmp/` に必ず保存

## cat4: 外部依存リリース監視

- 推奨ツール: `WebFetch`, `Read`
- チェック観点:
  - **Kotlin の最新 release**: `https://github.com/JetBrains/kotlin/releases`
  - **Compose Multiplatform の最新**: `https://github.com/JetBrains/compose-multiplatform/releases`
  - **Gradle の最新**: `https://services.gradle.org/versions/current`
  - **AGP の最新**: `https://developer.android.com/build/releases/gradle-plugin` または Maven Central
- 出力:
  - `gradle/libs.versions.toml` の現バージョンと比較
  - 1 マイナーバージョン以上の乖離があれば P2 として起票
  - breaking change がアナウンスされている場合は P1
- 注意:
  - `WebFetch` がレート制限に当たったら 1 回だけリトライ、 それでも駄目ならその事実を P3 として起票
  - 既存 PBT matrix（default / Kotlin 2.3.21 / Kotlin 2.4.0-Beta2）と差分があれば指摘

## cat5: 比較・残角度

- 推奨ツール: `WebSearch`, `Read`, `Grep`
- チェック観点:
  - **他 Kotlin Compiler Plugin との比較**: kotlinx.serialization / Compose Compiler / Metro 等の最近のアプローチ
  - **過去 PR / issue の残タスク**: `grep -rn 'TODO\\|FIXME' .claude/ docs/ README.md` などで埋もれた todo を拾う
  - **dev module の起動可否**: `./gradlew :dev:runHot --dry-run` などで設定的に壊れていないか
  - **BCP / SemVer 観点**: 直近 3 ヶ月で public API に変更があったら破壊的変更か確認
  - **locale / 大文字小文字**: 日本語 + 英語のテストデータで壊れる箇所
- 注意:
  - ここは「残った時間で雑に拾う」ゾーン。 P0/P1 を取りに行くより P2/P3 を 3〜4 件起票する方が価値が出やすい
