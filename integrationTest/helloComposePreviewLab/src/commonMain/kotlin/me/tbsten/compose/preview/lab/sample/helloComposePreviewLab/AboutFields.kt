package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.component.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.SelectableField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component.KotlinCodeBlock
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun AboutFields() = MaterialTheme(
    colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            AboutSection()

            FirstDemoSection(modifier = Modifier.heightIn(max = 600.dp))
        }
    }
}

@Composable
private fun AboutSection() {
    Column(
        modifier = Modifier.padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle(
            icon = { IconBox(color = Color(0xFF2196F3), label = "F") },
            text = "About Fields",
        )

        Text(
            text = "Fields allow you to dynamically change values passed to your Preview without modifying code or using PreviewParameterProvider.",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SectionTitle(
            icon = { IconBox(color = Color(0xFFFF9800), label = "?") },
            text = "Why use Fields instead of PreviewParameterProvider?",
            style = MaterialTheme.typography.titleMedium,
        )

        ComparisonTable()

        Spacer(modifier = Modifier.height(8.dp))

        SectionTitle(
            icon = { IconBox(color = MaterialTheme.colorScheme.primary, label = "!") },
            text = "Try it yourself below!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = "Open the Fields tab in the inspector panel → and interact with the controls to see how the preview updates in real-time.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SectionTitle(
    icon: @Composable () -> Unit,
    text: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = Color.Unspecified,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Text(
            text = text,
            style = style,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun IconBox(color: Color, label: String) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun ComparisonTable() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ComparisonRow(
            icon = { IconBox(color = Color(0xFF4CAF50), label = "✓") },
            title = "With Fields",
            description = "Change values dynamically via UI controls • Single preview • Easy to test edge cases",
        )

        HorizontalDivider()

        ComparisonRow(
            icon = { IconBox(color = Color(0xFFFF9800), label = "!") },
            title = "PreviewParameterProvider",
            description = "Multiple static previews • Increases cognitive load • Harder to test specific combinations",
        )
    }
}

@Composable
private fun ComparisonRow(icon: @Composable () -> Unit, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        icon()
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun FirstDemoSection(modifier: Modifier = Modifier) {
    PreviewLab(
        additionalTabs = listOf(FirstDemoFieldGuideTab),
        modifier = modifier
            .padding(40.dp)
            .shadow(8.dp),
    ) {
        FirstDemoItemList(
            headerText = fieldValue { StringField("headerText", initialValue = "Item List") },
            userScrollEnabled = fieldValue { BooleanField("userScrollEnabled", true) },
            items = fieldValue {
                SelectableField(label = "items") {
                    choice(emptyList(), label = "Empty")
                    choice(List(20) { "Item-$it" }, label = "20 Item", isDefault = true)
                }
            },
        )
    }
}

@Composable
private fun FirstDemoItemList(
    headerText: String,
    userScrollEnabled: Boolean,
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
            ) {
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            // List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No items to display",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = userScrollEnabled,
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "Item",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        }
                        if (item != items.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

private object FirstDemoFieldGuideTab : InspectorTab {
    override val title: String = "Guide"
    override val icon: @Composable () -> Painter = { ColorPainter(Color(0xFF6200EE)) }
    override val content: @Composable (PreviewLabState) -> Unit = { _ ->
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionTitle(
                    icon = { IconBox(color = Color(0xFF9C27B0), label = "G") },
                    text = "How to Use Fields",
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    text = "Follow these steps to interact with the Fields:",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Step(
                    number = "1",
                    title = "Open the Fields tab",
                    description = "Click on the 'Fields' tab in the inspector panel on the right.",
                )

                Step(
                    number = "2",
                    title = "Try StringField",
                    description = "Edit the 'headerText' field to see the header text change in real-time. This replaces the fixed string with a dynamic value using:",
                    code = """
                        fieldValue {
                          StringField(
                            label = "headerText",
                            initialValue = "Item List",
                          )
                        }
                    """.trimIndent(),
                )

                Step(
                    number = "3",
                    title = "Try BooleanField",
                    description = "Toggle 'userScrollEnabled' to enable/disable scrolling in the list. Notice how the boolean value controls the behavior:",
                    code = """
                        fieldValue {
                          BooleanField(
                            label = "userScrollEnabled",
                            initialValue = true,
                          )
                        }
                    """.trimIndent(),
                )

                Step(
                    number = "4",
                    title = "Try SelectableField",
                    description = "Change 'items' using the dropdown to switch between different sample data sets. SelectableField lets you choose from predefined options:",
                    code = $$"""
                        fieldValue {
                          SelectableField(label = "items") {
                            choice(emptyList(), label = "Empty")
                            choice(List(20) { "Item-${it}" }, label = "20 Item", isDefault = true)
                          }
                        }
                    """.trimIndent(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                SectionTitle(
                    icon = { IconBox(color = MaterialTheme.colorScheme.primary, label = "!") },
                    text = "Key Takeaway",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "With Fields, you can dynamically test different values, states, and edge cases without creating multiple Preview functions or using PreviewParameterProvider!",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun Step(number: String, title: String, description: String, code: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (code != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp)),
            ) {
                KotlinCodeBlock(
                    code = code,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    contentPadding = PaddingValues(8.dp),
                )
            }
        }
    }
}

@ComposePreviewLabOption(displayName = "2: About Fields", id = "AboutFields")
@Preview
@Composable
private fun AboutFieldsPreview() = Column {
    AboutFields()
}
