---
title: MCP Server
sidebar_position: 8
---

import KDocLink from '@site/src/components/KDocLink';

# MCP Server

:::warning Experimental
この機能は実験的です。今後のバージョンで API が変更される可能性があります。
:::

Compose Preview Lab は [MCP (Model Context Protocol)](https://modelcontextprotocol.io/) サーバを内蔵しており、AI アシスタント (Claude, Cursor など) から Preview の情報を取得・操作することができます。

現在 jvm プラットフォームでのみ利用できます。

## 概要

MCP Server を使用すると、以下のことが可能です：

- **[PreviewLabGallery](./preview-lab-gallery) の操作**: Preview の選択、検索クエリの変更、比較パネルへの追加
- **PreviewLab の操作**: [フィールド](./fields/overview)値の取得・更新、[イベント](./events)の取得・クリア、スクリーンショットの撮影

{/* TODO: MCP Server の概念図 (alt=MCP Server architecture diagram) */}

## セットアップ

### 1. PreviewLabGallery での有効化

`PreviewLabGalleryWindows` を使用している場合、MCP Server はデフォルトで有効です。

```kt
fun main() {
    application {
        PreviewLabGalleryWindows(
            previewList = app.PreviewList,
            // MCP Server はデフォルトで有効 (port: 7007)
        )
    }
}
```

### 2. MCP Client の設定

<details>
<summary>Claude Code の場合</summary>

以下のコマンドを実行：

```bash
claude mcp add compose-preview-lab --transport sse http://localhost:7007
```

</details>

<details>
<summary>Claude Desktop の場合</summary>

`claude_desktop_config.json` に以下を追加：

```json
{
  "mcpServers": {
    "compose-preview-lab": {
      "url": "http://localhost:7007"
    }
  }
}
```

</details>

<details>
<summary>Cursor の場合</summary>

`.cursor/mcp.json` に以下を追加：

```json
{
  "mcpServers": {
    "compose-preview-lab": {
      "url": "http://localhost:7007"
    }
  }
}
```

</details>

### 3. アプリケーションを起動する

PreviewLabGallery を使用したアプリケーションを起動すると、MCP Server も自動的に起動します。

## MCP Server の設定

`mcpServerConfig` パラメータで MCP Server の動作をカスタマイズできます。

```kt
PreviewLabGalleryWindows(
    previewList = app.PreviewList,
    mcpServerConfig = PreviewLabMcpServerConfig(
        enabled = true,       // MCP Server を有効化 (デフォルト: true)
        host = "127.0.0.1",   // バインドするホスト (デフォルト: "0.0.0.0")
        port = 7007,          // ポート番号 (デフォルト: 7007)
    ),
)
```

:::caution セキュリティに関する注意
デフォルト設定では `0.0.0.0` にバインドするため、ローカルネットワーク上のすべてのクライアントから MCP Server にアクセス可能です。MCP Server は認証なしで Preview の操作やスクリーンショット取得が可能なため、ローカル開発環境以外での使用時は `host = "127.0.0.1"` を設定してローカルホストのみに制限することを推奨します。
:::

### MCP Server を無効化する

```kt
PreviewLabGalleryWindows(
    previewList = app.PreviewList,
    mcpServerConfig = PreviewLabMcpServerConfig.Disable,
)
```

## 利用可能な Resources

### PreviewLabGallery Resources

| Resource | 説明 |
|----------|------|
| `preview-lab:///previews` | Gallery 内の全 Preview 一覧 |
| `preview-lab:///featuredFiles` | [Featured Files](./featured-files) の一覧 |
| `preview-lab:///gallery-state` | Gallery の現在の状態 (検索クエリ、選択中の Preview) |

### PreviewLab Resources

| Resource | 説明 |
|----------|------|
| `preview-lab:///preview-lab/list` | 利用可能な PreviewLab インスタンスの ID 一覧 |
| `preview-lab:///preview-lab/{previewId}/fields` | 指定した Preview のフィールド一覧 |
| `preview-lab:///preview-lab/{previewId}/events` | 指定した Preview のイベント一覧 |

## 利用可能な Tools

### PreviewLabGallery Tools

| Tool | 説明 |
|------|------|
| `Update PreviewLabGallery.query` | 検索クエリを更新 |
| `Select preview PreviewLabGallery.select` | Preview を選択して表示 |
| `Unselect preview PreviewLabGallery.unselect` | 選択を解除 |
| `Add to compare panel PreviewLabGallery.addToComparePanel` | 比較パネルに Preview を追加 |
| `Remove from compare panel PreviewLabGallery.removeFromComparePanel` | 比較パネルから Preview を削除 |

### PreviewLab Tools

| Tool | 説明 |
|------|------|
| `Update PreviewLab field` | フィールドの値を JSON シリアライズ値で更新 |
| `Update PreviewLab field with test value` | フィールドの値を testValues() の要素で更新 |
| `Take PreviewLab screenshot` | スクリーンショットを PNG 画像として取得 |
| `Clear PreviewLab events` | 記録されたイベントを全てクリア |

## 使用例

### AI Agent へ入力する prompt 例

#### Preview の確認

```
PreviewLabGallery で利用可能な Preview を確認して
```

#### フィールドの操作

```
MyButtonPreview の color フィールドを testValues の 2番目の値に変更して
```

#### スクリーンショットの取得

```
現在表示中の Preview のスクリーンショットを撮って
```

#### 複数 Preview の比較

```
MyButton の Primary と Secondary バリアントを比較パネルに追加して
```

{/* TODO: AI アシスタントでの操作例のスクリーンショット (alt=Example of AI assistant interacting with PreviewLab via MCP) */}

## 制限事項

- MCP Server は **JVM プラットフォーム** (Desktop) でのみ利用可能です
- MCP Server は **SSE (Server-Sent Events)** プロトコルを使用します
- スクリーンショット機能は Compose の `captureToImage()` を使用するため、一部の Composable では動作しない場合があります

## トラブルシューティング

### MCP Server に接続できない

- PreviewLabGallery が起動していることを確認
- ポート 7007 が他のアプリケーションで使用されていないか確認
- ファイアウォールの設定を確認

### Tools/Resources が見つからない

- MCP Client で Resources/Tools の一覧を再読み込みしてください。PreviewLab の Resources/Tools は Preview が表示されたタイミングで登録されます。
