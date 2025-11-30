package me.tbsten.compose.preview.lab.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.ColorField
import me.tbsten.compose.preview.lab.field.IntField
import me.tbsten.compose.preview.lab.field.ModifierField
import me.tbsten.compose.preview.lab.field.SelectableField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.modifier.ModifierFieldValue
import me.tbsten.compose.preview.lab.field.modifier.padding
import me.tbsten.compose.preview.lab.sample.component.previewLab
import org.jetbrains.compose.ui.tooling.preview.Preview

@ComposePreviewLabOption(id = "FieldCommonly")
@Preview
@Composable
private fun FieldCommonly() = previewLab {
    // StringField - TextField による文字列入力
    var userName by fieldState { StringField("userName", "John Doe") }

    // IntField - 繰り返し回数などで使用
    val itemCount = fieldValue { IntField("itemCount", 3) }

    // BooleanField - コンポーネントの有効/無効
    val buttonEnabled = fieldValue { BooleanField("buttonEnabled", true) }

    // SelectableField - リストから選択する
    val selectedFruit = fieldValue {
        SelectableField(label = "selectedFruit") {
            choice("🍎 Apple", label = "Apple", isDefault = true)
            choice("🍌 Banana", label = "Banana")
            choice("🍒 Cherry", label = "Cherry")
            choice("🍇 Grape", label = "Grape")
        }
    }

    // ColorField - 背景色などに適用
    val backgroundColor = fieldValue {
        ColorField("backgroundColor", Color(0xFF2196F3))
    }

    // ModifierField - Modifier として適用
    val boxModifier = fieldValue {
        ModifierField(
            label = "boxModifier",
            initialValue = ModifierFieldValue
                .padding(16.dp),
        )
    }

    Card(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Commonly Used Fields",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            HorizontalDivider()

            // StringField Example - TextField で使用
            FieldSection(
                title = "StringField",
                description = "String input for use in TextFields",
            ) {
                TextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("User Name") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                )
            }

            // IntField Example - 繰り返し回数で使用
            FieldSection(
                title = "IntField",
                description = "Used as a numerical value such as the number of repetitions",
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Item Count: $itemCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    repeat(itemCount) { index ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                Text("Item ${index + 1}")
                            }
                        }
                    }
                }
            }

            // BooleanField Example - Button の enabled で使用
            FieldSection(
                title = "BooleanField",
                description = "Toggle component enable/disable",
            ) {
                Button(
                    onClick = {},
                    enabled = buttonEnabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (buttonEnabled) "Button is Enabled" else "Button is Disabled")
                }
            }

            // SelectableField Example - 選択した内容を表示
            FieldSection(
                title = "SelectableField",
                description = "Use the value selected from the options",
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = selectedFruit,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Text(
                                text = "Selected Fruit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // ColorField Example - 背景色に適用
            FieldSection(
                title = "ColorField",
                description = "Apply to background colours, etc.",
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            backgroundColor,
                            RoundedCornerShape(8.dp),
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Background Color Applied",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // ModifierField Example - Modifier として適用
            FieldSection(
                title = "ModifierField",
                description = "Apply dynamic modifiers",
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp),
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = boxModifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp),
                            ),
                    ) {
                        Text(
                            text = "Box with dynamic Modifier",
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun FieldSection(title: String, description: String, content: @Composable () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
    }
}
