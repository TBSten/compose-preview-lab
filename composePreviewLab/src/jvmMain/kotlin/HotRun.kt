import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import me.tbsten.compose.preview.lab.me.PreviewLab
import me.tbsten.compose.preview.lab.me.PreviewLabConfiguration
import me.tbsten.compose.preview.lab.me.PreviewLabRoot
import me.tbsten.compose.preview.lab.me.field.IntField
import me.tbsten.compose.preview.lab.me.field.SelectableField
import me.tbsten.compose.preview.lab.me.field.StringField
import me.tbsten.compose.preview.lab.me.field.nullable
import me.tbsten.compose.preview.lab.me.testLayout

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

private val previewsForUiDebug = sequence<Pair<String, @Composable (() -> Unit)>> {
    yield("com.example.app.SimpleTextPreview" to {
        PreviewLab {
            Text("Hello SimpleTextPreview 🪩")
        }
    })

    yield("com.example.app.WithoutPreviewLab" to {
        Text("Hello SimpleTextPreview")
    })

    yield("com.example.app.Screen" to {
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
                            .testLayout("Preview$it")
                    )
                }
            }
        }
    })

    yield("com.example.app.Field" to {
        PreviewLab {
            Column {
                Text(fieldValue { IntField("Test", 123).nullable() }?.toString() ?: "(null)")

                Button(
                    onClick = { /* TODO("onEvent") */ }
                ) {
                    Text(fieldValue { StringField("Button.text", "Click Me") })
                }

                Box(
                    Modifier
                        .background(fieldValue {
                            threeColorField("coloredBox.color")
                        }).size(fieldValue {
                            IntField(
                                "coloredBox.size",
                                initialValue = 100,
                                suffix = { Text("dp") },
                            )
                        }.dp)
                )
            }
        }
    })

    yield("com.example.app.MultiConfigurations" to {
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
                        0f to fieldValue { threeColorField("color.start", initial = Color.Red) },
                        1f to fieldValue { threeColorField("color.end", initial = Color.Blue) })
                ).fillMaxSize()
            )
        }
    })
}

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
