---
title: "[TODO] Field の使用例"
sidebar_position: 5
---

:::warning

このページは生成 AI によって自動生成されたページです。

:::

# Field の使用例

このページでは、様々な Preview シナリオに対してどのような Field を設定すべきかの実例を紹介します。

## ボタンコンポーネント

ボタンコンポーネントでは、テキストや有効/無効状態を動的に変更できるようにします。

```kt
@Preview
@Composable
fun ButtonPreview() = PreviewLab {
    Button(
        onClick = { onEvent("onClick") },
        enabled = fieldValue { BooleanField("enabled", true) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = fieldValue { StringField("text", "Click Me") },
        )
    }
}
```

**使用する Field:**
- `StringField`: ボタンのテキスト
- `BooleanField`: ボタンの有効/無効状態

## フォーム入力コンポーネント

フォーム入力では、様々な型の値を入力できるようにします。

```kt
@Preview
@Composable
fun FormInputPreview() = PreviewLab {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = fieldValue { StringField("name", "John Doe") },
            onValueChange = {},
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
        )
        
        val age = fieldValue { IntField("age", 25) }
        OutlinedTextField(
            value = age.toString(),
            onValueChange = {},
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = fieldValue { BooleanField("agreed", false) },
                onCheckedChange = {},
            )
            Text("I agree to the terms")
        }
    }
}
```

**使用する Field:**
- `StringField`: テキスト入力（名前、メールアドレスなど）
- `IntField`: 数値入力（年齢、数量など）
- `BooleanField`: チェックボックスやスイッチ

## カード/リストアイテム

カードやリストアイテムでは、タイトル、説明、色、サイズなどを変更できるようにします。

```kt
@Preview
@Composable
fun CardPreview() = PreviewLab {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = fieldValue { 
                ColorField("backgroundColor", Color(0xFF6200EE)) 
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = fieldValue { StringField("title", "Card Title") },
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = fieldValue { 
                    StringField("description", "This is a card description")
                        .withTextHint()
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
```

**使用する Field:**
- `StringField`: タイトル、説明文
- `ColorField`: 背景色、テキスト色
- `.withTextHint()`: よく使うテキストパターンを追加

## ダイアログ

ダイアログでは、タイトル、メッセージ、ボタンの状態などを変更できるようにします。

```kt
@Preview
@Composable
fun DialogPreview() = PreviewLab {
    val showDialog = fieldValue { BooleanField("showDialog", true) }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = fieldValue { StringField("title", "Confirm") },
                )
            },
            text = {
                Text(
                    text = fieldValue { 
                        StringField("message", "Are you sure?")
                            .withTextHint()
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {}) {
                    Text(
                        text = fieldValue { StringField("confirmText", "OK") },
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {}) {
                    Text(
                        text = fieldValue { StringField("dismissText", "Cancel") },
                    )
                }
            },
        )
    }
}
```

**使用する Field:**
- `StringField`: タイトル、メッセージ、ボタンテキスト
- `BooleanField`: ダイアログの表示/非表示

## プロフィール表示

プロフィール表示では、名前、年齢、アバターの色などを変更できるようにします。

```kt
@Preview
@Composable
fun ProfilePreview() = PreviewLab {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = fieldValue { 
                        ColorField("avatarColor", Color(0xFF6200EE)) 
                    },
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = fieldValue { StringField("name", "John") }
                    .take(1)
                    .uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
        }
        
        Column {
            Text(
                text = fieldValue { StringField("name", "John Doe") },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${fieldValue { IntField("age", 25) }} years old",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
```

**使用する Field:**
- `StringField`: 名前
- `IntField`: 年齢
- `ColorField`: アバターの背景色

## 設定画面

設定画面では、スイッチ、選択肢、列挙型などを変更できるようにします。

```kt
enum class Theme {
    Light, Dark, System
}

@Preview
@Composable
fun SettingsPreview() = PreviewLab {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // BooleanField でスイッチ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Enable notifications")
            Switch(
                checked = fieldValue { BooleanField("notifications", true) },
                onCheckedChange = {},
            )
        }
        
        // SelectableField で選択肢
        val selectedLanguage = fieldValue {
            SelectableField(
                "language",
                listOf("English", "Japanese", "Spanish"),
                "English",
            )
        }
        Text("Language: $selectedLanguage")
        
        // EnumField でテーマ選択
        val theme = fieldValue {
            EnumField("theme", Theme.System)
        }
        Text("Theme: ${theme.name}")
    }
}
```

**使用する Field:**
- `BooleanField`: スイッチやトグル
- `SelectableField`: 選択肢から選ぶ
- `EnumField`: 列挙型の値

## リストアイテム

リストアイテムでは、タイトル、サブタイトル、アイコン、状態などを変更できるようにします。

```kt
@Preview
@Composable
fun ListItemPreview() = PreviewLab {
    ListItem(
        headlineContent = {
            Text(
                text = fieldValue { StringField("title", "List Item Title") },
            )
        },
        supportingContent = {
            Text(
                text = fieldValue { 
                    StringField("subtitle", "Supporting text")
                        .withTextHint()
                },
            )
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = fieldValue { 
                    ColorField("iconColor", MaterialTheme.colorScheme.primary) 
                },
            )
        },
        trailingContent = {
            if (fieldValue { BooleanField("showBadge", true) }) {
                Badge {
                    Text(
                        text = fieldValue { IntField("badgeCount", 5).toString() },
                    )
                }
            }
        },
    )
}
```

**使用する Field:**
- `StringField`: タイトル、サブタイトル
- `ColorField`: アイコンの色
- `BooleanField`: バッジの表示/非表示
- `IntField`: バッジの数値

## グラデーション背景

グラデーション背景では、開始色と終了色を変更できるようにします。

```kt
@Preview
@Composable
fun GradientBackgroundPreview() = PreviewLab {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        fieldValue { ColorField("startColor", Color(0xFF6200EE)) },
                        fieldValue { ColorField("endColor", Color(0xFF03DAC6)) },
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = fieldValue { StringField("text", "Gradient Background") },
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
    }
}
```

**使用する Field:**
- `ColorField`: グラデーションの開始色と終了色

## カスタムレイアウト

カスタムレイアウトでは、パディング、サイズ、配置などを変更できるようにします。

```kt
@Preview
@Composable
fun CustomLayoutPreview() = PreviewLab {
    val size = fieldValue { DpSizeField("size", DpSize(200.dp, 200.dp)) }
    val padding = fieldValue { DpField("padding", 16.dp) }
    val backgroundColor = fieldValue { ColorField("backgroundColor", Color.LightGray) }
    val cornerRadius = fieldValue { DpField("cornerRadius", 8.dp) }
    
    Box(
        modifier = Modifier
            .size(size)
            .padding(padding)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(cornerRadius),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = fieldValue { StringField("text", "Custom Layout") },
        )
    }
}
```

**使用する Field:**
- `DpSizeField`: 幅と高さ
- `DpField`: パディング、角丸の半径
- `ColorField`: 背景色

## まとめ

各コンポーネントの特性に応じて、適切な Field を選択することで、Preview をより柔軟にテストできるようになります。

**Field の選択のヒント:**
- **文字列**: `StringField` + `.withTextHint()` でよく使うパターンを追加
- **数値**: `IntField`, `FloatField`, `DoubleField` など型に応じて選択
- **真偽値**: `BooleanField` でスイッチやトグルを制御
- **色**: `ColorField` で色を視覚的に選択
- **サイズ**: `DpField`, `DpSizeField` でサイズを調整
- **選択肢**: `SelectableField` や `EnumField` で選択肢から選ぶ

詳細は [All Fields](./all-fields) を参照してください。

