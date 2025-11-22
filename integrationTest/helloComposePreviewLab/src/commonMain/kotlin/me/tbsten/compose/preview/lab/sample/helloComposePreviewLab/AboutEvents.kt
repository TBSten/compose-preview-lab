package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.LocalPreviewLabGalleryNavigator
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.PreviewLabScope
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.component.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component.DocPage
import me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component.IconBox
import me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component.KotlinCodeBlock
import me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component.createCodeBlockColor
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun AboutEvents() {
    DocPage {
        AboutSection()

        FeaturesSection()

        TryItYourselfSection()

        TitleVsDescriptionSection()

        PracticalExamplesSection()
    }
}

@Composable
private fun AboutSection() {
    Column(
        modifier = Modifier.padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle(
            icon = { IconBox(color = Color(0xFFFF6B6B), label = "E") },
            text = "About Events",
        )

        Text(
            text = "Events allow you to record and visualize user interactions (like clicks, value changes, etc.) in your Preview without complex testing infrastructure.",
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(
            text = "When onEvent is called, a Toast appears immediately and the event is logged in the Events tab for review.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@OptIn(InternalComposePreviewLabApi::class)
@Composable
private fun TryItYourselfSection() {
    Column(
        modifier = Modifier.padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle(
            icon = { IconBox(color = MaterialTheme.colorScheme.primary, label = "!") },
            text = "Try it yourself below!",
        )

        Text(
            text = "Click the button in the preview below and check the Events tab in the inspector panel → to see how events are recorded.",
            style = MaterialTheme.typography.bodyMedium,
        )

        CompositionLocalProvider(
            LocalPreviewLabGalleryNavigator provides null,
            LocalOpenFileHandler provides null,
        ) {
            PreviewLab(
                additionalTabs = listOf(FirstDemoGuideTab),
                isHeaderShow = false,
                modifier = Modifier
                    .padding(40.dp)
                    .shadow(8.dp)
                    .height(350.dp),
            ) {
                FirstDemoContent()
            }
        }
    }
}

@Composable
private fun PreviewLabScope.FirstDemoContent() {
    var clickCount by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Click Count: $clickCount",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    clickCount++
                    onEvent("Button Clicked")
                },
                modifier = Modifier.fillMaxWidth(0.6f),
            ) {
                Text("Click Me!")
            }
        }
    }
}

private object FirstDemoGuideTab : InspectorTab {
    override val title: String = "Guide"
    override val icon: @Composable () -> Painter = { ColorPainter(Color(0xFFFF6B6B)) }
    override val content: @Composable (PreviewLabState) -> Unit = { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionTitle(
                icon = { IconBox(color = Color(0xFF9C27B0), label = "G") },
                text = "How to Use Events",
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = "Follow these steps to see events in action:",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Step(
                number = "1",
                title = "Click the button",
                description = "Notice the Toast notification that appears immediately.",
            )

            Step(
                number = "2",
                title = "Open the Events tab",
                description = "Switch to the 'Events' tab in the inspector panel to see the event history.",
            )

            Step(
                number = "3",
                title = "Review the event log",
                description = "Each click is recorded with a timestamp and title, making it easy to track user interactions.",
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Code:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    KotlinCodeBlock(
                        code = """
                            Button(
                              onClick = {
                                clickCount++
                                onEvent("Button Clicked")
                              }
                            ) {
                              Text("Click Me!")
                            }
                        """.trimIndent(),
                    )
                }
            }
        }
    }
}

@Composable
private fun Step(number: String, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FeaturesSection() {
    Column(
        modifier = Modifier.padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle(
            icon = { IconBox(color = Color(0xFF4CAF50), label = "✓") },
            text = "Key Features",
        )

        Text(
            text = "Here's the minimal code to use onEvent in your Preview:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KotlinCodeBlock(
                    code = """
                        PreviewLab {
                          Button(
                            onClick = { onEvent("Button Clicked") }
                          ) {
                            Text("Click Me")
                          }
                        }
                    """.trimIndent(),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FeatureItem(
                    icon = { IconBox(color = Color(0xFFFF9800), label = "T") },
                    title = "Toast Notifications",
                    description = "Get immediate visual feedback when an event occurs",
                )

                HorizontalDivider()

                FeatureItem(
                    icon = { IconBox(color = Color(0xFF2196F3), label = "L") },
                    title = "Event Log",
                    description = "All events are recorded in the Events tab with timestamps",
                )

                HorizontalDivider()

                FeatureItem(
                    icon = { IconBox(color = Color(0xFF9C27B0), label = "D") },
                    title = "Debug Information",
                    description = "Add detailed descriptions for complex events without cluttering the UI",
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(icon: @Composable () -> Unit, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        icon()
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TitleVsDescriptionSection() {
    Column(
        modifier = Modifier.padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle(
            icon = { IconBox(color = Color(0xFFFF9800), label = "?") },
            text = "Title vs Description",
        )

        Text(
            text = "Understanding when to use title and description helps keep your event log clean and informative:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .background(createCodeBlockColor())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TitleDescriptionRow(
                    icon = { IconBox(color = Color(0xFF4CAF50), label = "T") },
                    label = "Title",
                    description = "Short, clear event name • Shown in Toast • Appears in event list",
                )

                HorizontalDivider()

                TitleDescriptionRow(
                    icon = { IconBox(color = Color(0xFF2196F3), label = "D") },
                    label = "Description",
                    description = "Detailed information • NOT shown in Toast • Only in Events tab • Great for parameters and state",
                )
            }
        }
    }
}

@Composable
private fun TitleDescriptionRow(icon: @Composable () -> Unit, label: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        icon()
        Column {
            Text(
                text = label,
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
private fun PracticalExamplesSection() {
    Column(
        modifier = Modifier.padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionTitle(
            icon = { IconBox(color = Color(0xFF9C27B0), label = "C") },
            text = "Practical Examples",
        )

        Text(
            text = "Here are common patterns for using onEvent in your Previews:",
            style = MaterialTheme.typography.bodyMedium,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ExampleCard(
                title = "Simple Event",
                description = "Basic event with just a title",
                code = """
                    Button(
                      onClick = { onEvent("Button Clicked") }
                    ) {
                      Text("Click Me")
                    }
                """.trimIndent(),
            )

            ExampleCard(
                title = "Event with Parameters",
                description = "Track events with detailed information",
                code = """
                    TextField(
                      value = text,
                      onValueChange = { newValue ->
                        setText(newValue)
                        onEvent(
                          title = "Text Changed",
                          description = "New value: ${'$'}newValue"
                        )
                      }
                    )
                """.trimIndent(),
            )

            ExampleCard(
                title = "Complex Event",
                description = "Record events with structured data",
                code = """
                    SubmitButton(
                      onClick = {
                        onEvent(
                          title = "Form Submitted",
                          description = ${"\"\"\""}
                            Name: ${'$'}{form.name}
                            Email: ${'$'}{form.email}
                            Age: ${'$'}{form.age}
                          ${"\"\"\""}${""}.trimIndent()
                        )
                      }
                    )
                """.trimIndent(),
            )
        }
    }
}

@Composable
private fun ExampleCard(title: String, description: String, code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            KotlinCodeBlock(code = code)
        }
    }
}

@ComposePreviewLabOption(displayName = "3: About Events", id = "AboutEvents")
@Preview
@Composable
private fun AboutEventsPreview() {
    AboutEvents()
}
