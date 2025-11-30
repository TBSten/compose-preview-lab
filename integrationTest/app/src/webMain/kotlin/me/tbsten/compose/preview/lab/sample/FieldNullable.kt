package me.tbsten.compose.preview.lab.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.field.ColorField
import me.tbsten.compose.preview.lab.field.IntField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.nullable
import me.tbsten.compose.preview.lab.component.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.sample.component.previewLab
import me.tbsten.compose.preview.lab.sample.component.rememberCodeTab
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@ComposePreviewLabOption(id = "FieldNullable")
@Composable
private fun FieldNullable() = previewLab(
    inspectorTabs = InspectorTab.defaults + listOf(
        rememberCodeTab(
            code = """
                // Make StringField nullable for optional bio
                val bio: String? = fieldValue {
                    StringField("bio", "I love coding!")
                        .nullable(initialValue = null)
                }

                // Make IntField nullable for optional timeout
                val timeout: Int? = fieldValue {
                    IntField("timeout", 30)
                        .nullable()
                }

                // Make ColorField nullable for optional custom color
                val customColor: Color? = fieldValue {
                    ColorField("customColor", Color.Blue)
                        .nullable(initialValue = null)
                }
            """.trimIndent(),
        ),
    ),
) {
    // StringField.nullable() - Optional bio text
    val bio: String? = fieldValue {
        StringField("bio", "I love coding and building great products!")
            .nullable(initialValue = null)
    }

    // StringField.nullable() - Optional avatar URL
    val avatarUrl: String? = fieldValue {
        StringField("avatarUrl", "https://example.com/avatar.jpg")
            .nullable(initialValue = null)
    }

    // IntField.nullable() - Optional timeout configuration
    val timeout: Int? = fieldValue {
        IntField("timeout", 30)
            .nullable()
    }

    // ColorField.nullable() - Optional badge color
    val badgeColor: Color? = fieldValue {
        ColorField("badgeColor", Color(0xFFFF9800))
            .nullable(initialValue = null)
    }

    // IntField.nullable() - Optional max items
    val maxItems: Int? = fieldValue {
        IntField("maxItems", 5)
            .nullable(initialValue = null)
    }

    Card(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // User Profile Example
            FieldSection(
                title = "User Profile Card",
                description = "Optional bio and avatar URL using StringField.nullable()",
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    if (avatarUrl != null) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (avatarUrl != null) "IMG" else "?",
                                color = if (avatarUrl != null) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Text(
                            text = "John Doe",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        // Bio (nullable)
                        if (bio != null) {
                            Text(
                                text = bio,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                text = "No bio available",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Timeout Configuration Example
            FieldSection(
                title = "API Configuration",
                description = "Optional timeout setting using IntField.nullable()",
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Request Timeout",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = if (timeout != null) "${timeout}s" else "No Limit",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (timeout != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            text = if (timeout != null) {
                                "API requests will timeout after $timeout seconds"
                            } else {
                                "API requests will wait indefinitely (no timeout)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Badge Color Example
            FieldSection(
                title = "Badge with Custom Color",
                description = "Optional badge color using ColorField.nullable()",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp),
                            )
                            .padding(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Notifications",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            if (badgeColor != null) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "5",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            } else {
                                Text(
                                    text = "(no badge)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            // Max Items Example
            FieldSection(
                title = "Item List with Optional Limit",
                description = "Optional max items using IntField.nullable()",
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (maxItems != null) {
                            "Showing up to $maxItems items"
                        } else {
                            "Showing all items (no limit)"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    val totalItems = 10
                    val itemsToShow = maxItems?.coerceAtMost(totalItems) ?: totalItems

                    repeat(itemsToShow) { index ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                Text("Item ${index + 1}")
                            }
                        }
                    }

                    if (maxItems != null && maxItems < totalItems) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp),
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "... and ${totalItems - maxItems} more items",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
