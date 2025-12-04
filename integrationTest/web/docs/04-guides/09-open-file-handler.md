---
title: OpenFileHandler
sidebar_position: 9
---

@04-collect-preview.md (5-10)

# OpenFileHandler

`OpenFileHandler` は、PreviewLab の UI からソースコードファイルを開くためのハンドラーです。GitHub などのリポジトリホスティングサービスや IDE へのリンクを生成し、ユーザーが簡単にソースコードを確認できるようにします。

## 概要

PreviewLab では、各 Preview のファイルパスと行番号の情報が保持されています。`OpenFileHandler` を設定することで、これらの情報を使って外部のエディタや Web ブラウザでソースコードを開くことができます。

## 基本的な使い方

`PreviewLabGallery` または `previewLabApplication` の `openFileHandler` パラメータに `OpenFileHandler` のインスタンスを渡します。

```kt
fun main() = previewLabApplication(
    previewList = app.PreviewList,
    openFileHandler = GithubOpenFileHandler(
        githubRepository = "owner/repository",
        branch = "main",
    ),
)
```

## 組み込みの OpenFileHandler

### GithubOpenFileHandler

GitHub リポジトリのファイルを開くための `OpenFileHandler` です。

```kt
val githubHandler = GithubOpenFileHandler(
    githubRepository = "owner/repository",  // 必須: "owner/repo" 形式
    branch = "main",                        // オプション: デフォルトは "main"
    server = "https://github.com",         // オプション: GitHub Enterprise の場合に変更
)

PreviewLabGallery(
    previewList = app.PreviewList,
    openFileHandler = githubHandler,
)
```

### UrlOpenFileHandler

任意のベース URL からファイルを開くための汎用的な `OpenFileHandler` です。

```kt
val urlHandler = UrlOpenFileHandler(
    baseUrl = "https://github.com/owner/repo/blob/main/"
)

PreviewLabGallery(
    previewList = app.PreviewList,
    openFileHandler = urlHandler,
)
```

URL は `$baseUrl${filePathInProject}${startLineNumberがある場合は "#L${startLineNumber}"}` の形式で生成されます。

例：
- `baseUrl = "https://github.com/owner/repo/blob/main/"`
- `filePathInProject = "src/commonMain/kotlin/MyComponent.kt"`
- `startLineNumber = 42`
- 生成される URL: `https://github.com/owner/repo/blob/main/src/commonMain/kotlin/MyComponent.kt#L42`

## カスタム OpenFileHandler の作成

独自の `OpenFileHandler` を作成するには、`OpenFileHandler` インターフェースを実装します。

### シンプルな実装

Composable 関数内で設定が必要ない場合は、`OpenFileHandler` 関数を使用します。

```kt
val customHandler = OpenFileHandler { params ->
    // params.filePathInProject: プロジェクト内のファイルパス
    // params.startLineNumber: 開始行番号（null の可能性あり）
    
    // ファイルを開く処理を実装
    println("Opening: ${params.filePathInProject} at line ${params.startLineNumber}")
    // 例: IDE の API を呼び出す、URL を開くなど
}
```

### Composable 関数内で設定が必要な場合

`LocalUriHandler` などの Composable 関数内で取得できる値が必要な場合は、インターフェースを直接実装します。

```kt
class CustomOpenFileHandler : OpenFileHandler<UriHandler> {
    @Composable
    override fun configure(): UriHandler {
        // Composable 関数内で取得できる値を返す
        return LocalUriHandler.current
    }

    override fun openFile(params: OpenFileHandler.Params<UriHandler>) {
        // configure() で取得した値を使用
        val uriHandler = params.configuredValue
        
        // カスタムロジックでファイルを開く
        val url = "custom://${params.filePathInProject}?line=${params.startLineNumber}"
        uriHandler.openUri(url)
    }
}
```

## 使用例

### GitHub リポジトリへのリンク

```kt
fun main() = previewLabApplication(
    previewList = app.PreviewList,
    openFileHandler = GithubOpenFileHandler(
        githubRepository = "TBSten/compose-preview-lab",
        branch = "main",
    ),
)
```

### GitLab へのリンク

```kt
fun main() = previewLabApplication(
    previewList = app.PreviewList,
    openFileHandler = UrlOpenFileHandler(
        baseUrl = "https://gitlab.com/owner/repo/-/blob/main/",
    ),
)
```

### IDE へのリンク（例: IntelliJ IDEA）

```kt
val ideHandler = OpenFileHandler { params ->
    val filePath = params.filePathInProject
    val lineNumber = params.startLineNumber ?: 1
    
    // IntelliJ IDEA の URL スキームを使用
    val url = "idea://open?file=${filePath}&line=${lineNumber}"
    // プラットフォームに応じて URL を開く処理を実装
}
```

## OpenFileHandler.Params

`OpenFileHandler.openFile()` メソッドには `OpenFileHandler.Params` が渡されます。このクラスには以下のプロパティが含まれています：

- `configuredValue: T` - `configure()` メソッドで設定された値
- `filePathInProject: String` - プロジェクトルートからの相対パス
- `startLineNumber: Int?` - Preview の開始行番号（存在しない場合は `null`）

## トラブルシューティング

### ファイルパスが正しくない

`filePathInProject` はプロジェクトルートからの相対パスです。`build.gradle.kts` の `composePreviewLab` ブロックで `projectRootPath` を設定することで、パスの解決方法を調整できます。

```kts
composePreviewLab {
    projectRootPath = project.rootProject.projectDir.absolutePath
}
```

詳細は [Build Settings](./07-build-settings) を参照してください。

### URL が正しく生成されない

`UrlOpenFileHandler` を使用する場合、`baseUrl` の末尾に `/` を含めるかどうかに注意してください。

- ✅ `baseUrl = "https://github.com/owner/repo/blob/main/"` （末尾に `/` あり）
- ❌ `baseUrl = "https://github.com/owner/repo/blob/main"` （末尾に `/` なし）

## 関連リンク

- [PreviewLabGallery](./05-preview-lab-gallery) - `openFileHandler` の使用方法
- [Collect Preview](./04-collect-preview) - ファイルパス情報の生成方法
- [Build Settings](./07-build-settings) - `projectRootPath` の設定

