import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import me.tbsten.compose.preview.lab.me.CollectedPreview
import me.tbsten.compose.preview.lab.me.PreviewLab
import me.tbsten.compose.preview.lab.me.PreviewLabConfiguration
import me.tbsten.compose.preview.lab.me.PreviewLabRoot
import me.tbsten.compose.preview.lab.me.field.IntField
import me.tbsten.compose.preview.lab.me.field.SelectableField
import me.tbsten.compose.preview.lab.me.field.StringField
import me.tbsten.compose.preview.lab.me.field.nullable
import me.tbsten.compose.preview.lab.me.layoutLab

fun threeColorField(
    label: String,
    initial: Color = Color.Red,
): SelectableField<Color> {
    val colors = listOf(
        Color.Red,
        Color.Blue,
        Color.Green,
    )
    return SelectableField(
        label = label,
        choices = colors,
        initialValue = initial
    )
}

private val previewsForUiDebug =
    listOf<CollectedPreview>(
        CollectedPreview("com.example.app.SimpleTextPreview") {
            PreviewLab {
                Text("Hello SimpleTextPreview 🪩")
            }
        },
        CollectedPreview("com.example.app.WithoutPreviewLab") {
            Text("Hello SimpleTextPreview")
        },
        CollectedPreview("com.example.app.Screen") {
            PreviewLab {
                Column(
                    modifier = Modifier
                        .background(Color.Gray)
                        .fillMaxSize()
                ) {
                    repeat(4) {
                        Text(
                            "Preview$it",
                            modifier = Modifier
                                .layoutLab("Preview$it")
                        )
                    }
                }
            }
        },
        CollectedPreview("com.example.app.Field") {
            PreviewLab {
                Column {
                    Text(fieldValue { IntField("Test", 123).nullable() }?.toString() ?: "(null)")

                    Button(
                        onClick = { onEvent("Click Button") }
                    ) {
                        Text(fieldValue { StringField("Button.text", "Click Me") })
                    }

                    Box(
                        Modifier
                            .background(fieldValue {
                                threeColorField("coloredBox.color")
                            }).size(fieldValue {
                                IntField(
                                    label = "coloredBox.size",
                                    initialValue = 100,
                                    inputType = IntField.InputType.TextField(suffix = { Text("dp") }),
                                )
                            }.dp)
                    )
                }
            }
        },
        CollectedPreview("com.example.app.Layout") {
            PreviewLab {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .layoutLab("Column")
                        .padding(20.dp)
                ) {
                    Box(
                        Modifier
                            .layoutLab("Red")
                            .background(Color.Red)
                            .size(20.dp)
                    )

                    Box(
                        Modifier
                            .layoutLab("Blue")
                            .background(Color.Blue)
                            .size(fieldValue { IntField("Blue.size", 20) }.dp)
                    )

                    Box(
                        Modifier
                            .layoutLab("Green")
                            .background(Color.Green)
                            .size(20.dp)
                    )
                }
            }
        },
        CollectedPreview("com.example.app.MultiConfigurations") {
            PreviewLab(
                configurations = listOf(
                    PreviewLabConfiguration(
                        name = "small device",
                        maxWidth = 300.dp,
                        maxHeight = 500.dp,
                    ),
                    PreviewLabConfiguration(
                        name = "middle device",
                        maxWidth = 420.dp,
                        maxHeight = 600.dp,
                    ),
                    PreviewLabConfiguration(
                        name = "large device",
                        maxWidth = 680.dp,
                        maxHeight = 800.dp,
                    ),
                    PreviewLabConfiguration(
                        name = "fit component size",
                        maxWidth = null,
                        maxHeight = null,
                    ),
                )
            ) {
                Box(
                    Modifier.background(
                        Brush.linearGradient(
                            0f to fieldValue {
                                threeColorField(
                                    "color.start",
                                    initial = Color.Red
                                )
                            },
                            1f to fieldValue { threeColorField("color.end", initial = Color.Blue) })
                    ).fillMaxSize()
                )
            }
        }
    )

private fun main(): Unit = singleWindowApplication(
    title = "Compose Preview Lab UI",
) {
    PreviewLabRoot(
        previews = previewsForUiDebug,
        openFileHandler = {
            println("🪩 PreviewLabRoot.openFileHandler")
        },
    )
}
