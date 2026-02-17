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
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.ColorField
import me.tbsten.compose.preview.lab.field.IntField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.withHint
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.sample.component.SamplePreviewLab
import me.tbsten.compose.preview.lab.sample.component.rememberCodeTab
import androidx.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "FieldWithHint")
@Composable
private fun FieldWithHint() = SamplePreviewLab(
    inspectorTabs = InspectorTab.defaults +
        listOf(
            rememberCodeTab(
                code = """
                // Provide commonly used string hints with StringField
                val email = fieldValue {
                    StringField("email", "user@example.com")
                        .withHint(
                            "User" to "user@example.com",
                            "Admin" to "admin@example.com",
                            "Test" to "test@example.com",
                        )
                }

                // Provide commonly used number hints with IntField
                val fontSize = fieldValue {
                    IntField("fontSize", 16)
                        .withHint(
                            "Small" to 12,
                            "Medium" to 16,
                            "Large" to 20,
                            "XLarge" to 24,
                        )
                }

                // Provide theme color hints with ColorField
                val color = fieldValue {
                    ColorField("color", Color.Blue)
                        .withHint(
                            "Primary" to Color(0xFF2196F3),
                            "Success" to Color(0xFF4CAF50),
                            "Warning" to Color(0xFFFFC107),
                            "Error" to Color(0xFFF44336),
                        )
                }
                """.trimIndent(),
            ),
        ),
) {
    // StringField with hint - email address hints
    var email by fieldState {
        StringField("email", "user@example.com")
            .withHint(
                "User" to "user@example.com",
                "Admin" to "admin@example.com",
                "Test" to "test@example.com",
                "Guest" to "guest@example.com",
            )
    }

    // StringField with hint - username hints
    val username = fieldValue {
        StringField("username", "john_doe")
            .withHint(
                "John" to "john_doe",
                "Alice" to "alice_wonder",
                "Bob" to "bob_builder",
                "Charlie" to "charlie_choco",
            )
    }

    // ColorField with hint - theme color hints
    val themeColor = fieldValue {
        ColorField("themeColor", Color(0xFF2196F3))
            .withHint(
                "Primary Blue" to Color(0xFF2196F3),
                "Success Green" to Color(0xFF4CAF50),
                "Warning Orange" to Color(0xFFFFC107),
                "Error Red" to Color(0xFFF44336),
                "Purple" to Color(0xFF9C27B0),
            )
    }

    // IntField with hint - item count hints
    val itemCount = fieldValue {
        IntField("itemCount", 5)
            .withHint(
                "Empty" to 0,
                "Few" to 3,
                "Normal" to 5,
                "Many" to 10,
                "Lots" to 20,
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
                text = "Field with Hint Examples",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Using .withHint(), hint choices appear below the field and you can quickly apply values by clicking them.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()

            // Email Example
            FieldSection(
                title = "StringField with Email Hints",
                description = "Provide commonly used email addresses as hints",
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Username Example
            FieldSection(
                title = "StringField with Username Hints",
                description = "Provide commonly used usernames as hints",
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "@$username",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Username",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Item Count Example
            FieldSection(
                title = "IntField with Item Count Hints",
                description = "Provide commonly used item counts as hints",
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Items: $itemCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    if (itemCount == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp),
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No items",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
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
            }

            // Theme Color Example
            FieldSection(
                title = "ColorField with Theme Color Hints",
                description = "Provide commonly used theme colors as hints",
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            themeColor,
                            RoundedCornerShape(8.dp),
                        )
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Theme Color Preview",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
