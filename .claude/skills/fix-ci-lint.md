# fix-ci-lint

Kotlin リントチェック（ktlintCheck）の診断と修正スキル

## 目的

`./gradlew ktlintCheck` が失敗した場合、エラーを診断して修正する。

## 問題の診断

### Step 1: エラーの確認

**実行コマンド**:

```bash
./gradlew ktlintCheck
```

**エラーメッセージから把握すべき情報**:

- どのファイルに違反があるか
- どのルールに違反しているか（インデント、命名規則等）
- 具体的な行番号

### Step 2: ローカル再現

失敗が再現できることを確認:

```bash
./gradlew ktlintCheck
```

## 修正手順

### パターン A: コードフォーマット違反

**修正方法**:

1. `./gradlew ktlintFormat` を実行してコードを自動フォーマット
2. 変更されたファイルを確認
3. 変更に問題がないか視覚的に確認
4. 再度 `./gradlew ktlintCheck` を実行して確認

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck
```

### パターン B: カスタムルール違反

ktlint は `ktlint.gradle.kts` などの設定ファイルでカスタムルールが定義されている場合がある。

1. `build.gradle.kts` または `ktlint.gradle.kts` を確認
2. ルール定義を理解
3. 違反しているコードを Read で確認
4. Edit を使用して修正

## 検証

修正後、以下のコマンドで確認:

```bash
./gradlew ktlintCheck
```

リントチェックが成功すること（終了コード 0）を確認。
