# fix-ci-setup

CI/CD 環境構築とセットアップ診断スキル

## 目的

GitHub Actions CI/CD パイプラインの設定を確認し、環境構築上の問題を診断・修正する。

## 実行手順

### Step 1: CI 設定ファイルの確認

**必ず最初に実行**:

1. `.github/workflows/` ディレクトリの存在確認
   - ツール: Glob で `.github/workflows/*.yml` を検索
2. 各ワークフローファイルを読み込んで内容を把握
   - ツール: Read で各ワークフローファイルを読み込む
   - 特に `pull-request.yml` の内容を詳細に確認

**確認すべき内容**:

- どのジョブが存在するか
- 各ジョブで実行されるコマンド
- マトリックス戦略の設定
- 環境変数やシークレットの設定

### Step 2: ワークフロー構造の理解

**pull-request.yml** の主要ジョブ:

1. **lint**: `./gradlew ktlintCheck`
   - Kotlin コードのリントチェック
2. **validate-binary-compatibility**: `./gradlew apiCheck`
   - バイナリ互換性の検証
3. **test**: マトリックス戦略で複数のプラットフォームでテスト
   - Jvm, JS, Wasm JS, Android Debug/Release, iOS
4. **publish**: Maven Local への公開

### Step 3: 環境情報の確認

ローカル環境の情報を確認:

```bash
java -version
./gradlew --version
```

Windows vs macOS vs Linux での差異を考慮する。

## トラブルシューティング

### CI 設定ファイルが見つからない場合

1. Glob でプロジェクトルートを確認
2. `.github/workflows/*.yml` を検索
3. 存在しない場合は、プロジェクトがまだ CI を使用していない可能性を報告

### 環境変数/シークレットの不足

1. GitHub Secrets が適切に設定されているか確認
2. ワークフローファイルで参照されている変数を確認
3. 不足している場合は、ユーザーに通知して設定を依頼
