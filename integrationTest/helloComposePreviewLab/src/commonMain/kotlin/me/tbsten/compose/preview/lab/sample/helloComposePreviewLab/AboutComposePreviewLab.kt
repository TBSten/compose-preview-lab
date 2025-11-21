package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose_preview_lab_integration_test.hellocomposepreviewlab.generated.resources.Res
import compose_preview_lab_integration_test.hellocomposepreviewlab.generated.resources.cover
import compose_preview_lab_integration_test.hellocomposepreviewlab.generated.resources.icon_add_notes
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.LocalPreviewLabGalleryNavigator
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.PreviewLabState
import me.tbsten.compose.preview.lab.component.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.navigateOr
import me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component.KotlinCodeBlock
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// FIXME migrate LabDoc API

@Composable
internal fun AboutComposePreviewLab() = MaterialTheme(
    colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            CoverSection()

            QuickSummarySection()

            BeforeAfterSection()

            NextActionSection()
        }
    }
}

private object SingletonStore {
    private val storedDataMap = mutableMapOf<Any?, Any?>()

    @Composable
    fun <T> stored(key: Any? = null, block: () -> T): T {
        val key = key ?: currentCompositeKeyHashCode.toString()
        return if (storedDataMap.containsKey(key)) {
            storedDataMap[key] as T
        } else {
            block()
                .also { storedDataMap[key] = it }
        }
    }
}

@Composable
private fun CoverSection() = Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    val cover by Res.drawable.cover.preloadImageVector()
    val uriHandler = LocalUriHandler.current
    val githubUrl = "https://github.com/TBSten/compose-preview-lab"

    val coverImageTransition =
        SingletonStore.stored { MutableTransitionState<ImageBitmap?>(null) }
            .apply { targetState = cover }

    rememberTransition(coverImageTransition).AnimatedContent(
        transitionSpec = {
            val enter =
                fadeIn(tween(300, 100)) +
                    scaleIn(tween(600, easing = EaseOutElastic))
            val exit = fadeOut(tween(150))
            enter togetherWith exit
        },
    ) { coverImage ->
        if (coverImage != null) {
            Image(
                bitmap = coverImage,
                contentDescription = "Compose Preview Lab",
                modifier = Modifier
                    .clickable { uriHandler.openUri(githubUrl) }
                    .clip(RoundedCornerShape(8.dp))
                    .aspectRatio(391f / 220)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
            )
        } else {
            Box(
                Modifier
                    .background(Color(0xffa3a3a3))
                    .aspectRatio(391f / 220)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
            )
        }
    }

    Spacer(Modifier.height(8.dp))

    Text(
        text = githubUrl,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable { uriHandler.openUri(githubUrl) },
    )
}

@Composable
internal expect fun DrawableResource.preloadImageVector(): State<ImageBitmap?>

@Composable
private fun QuickSummarySection() = Column {
    SectionHeadingText(
        text = "Quick Summary",
    )

    Text(
        text = """
            ・Collect and display @Preview!
            ・Turn @Preview into a powerful Playground with just a little code!
        """.trimIndent(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeforeAfterSection() = Column {
    SectionHeadingText(
        text = "What is Compose Preview Lab ?",
    )

    val windowWidth =
        with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.width.toDp()
        }
    val isSmall = windowWidth <= 950.dp

    // FIXME before/after の例に 説明用の tooltip を追加してください
    //  ex. before: 何も起こりません
    //      after : ここをクリックしてみてください。

    val before = remember {
        movableContentOf {
            BeforeAfterCodeSection(
                textColor = Color(0xffff4444),
                backgroundColor = Color(0xffffe0e0),
                label = "before",
                code = """
                    @Preview
                    @Composable
                    private fun MyButtonPreview() {
                        MyButton(
                            text = "Click Me !",
                            isEnable = true,
                            onClick = { },
                        )
                    }
                """.trimIndent(),
                content = {
                    Box(
                        modifier = Modifier
                            .padding(20.dp),
                    ) {
                        Button(onClick = {}) {
                            Text("Click Me !")
                        }
                    }
                },
            )
        }
    }
    val after = remember {
        movableContentOf {
            // TODO highlight PreviewLab { }, fieldValue { }, onEvent

            BeforeAfterCodeSection(
                textColor = Color(0xff0a7119),
                backgroundColor = Color(0xffc8edb7),
                label = "after",
                code = """
                    @Preview
                    @Composable
                    private fun MyButtonPreview() = PreviewLab(
                        additionalTabs = listOf(MyCustomTab()) // ← Custom tab!
                    ) {
                        MyButton(
                            text = fieldValue { StringField("text", "Click Me !") },
                            isEnable = fieldValue { BooleanField("isEnable", true) },
                            onClick = { onEvent("onClick") },
                        )
                    }
                """.trimIndent(),
                content = {
                    PreviewLab(
                        isHeaderShow = false,
                        additionalTabs = listOf(CustomizedInfoTab),
                        modifier = Modifier.height(600.dp),
                    ) {
                        Button(
                            enabled = fieldValue { BooleanField("isEnable", true) },
                            onClick = { onEvent("onClick") },
                        ) {
                            Text(text = fieldValue { StringField("text", "Click Me !") })
                        }
                    }
                },
            )
        }
    }

    if (isSmall) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .height(1000.dp)
                .fillMaxWidth(),
        ) {
            before()
            after()
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .height(600.dp)
                .widthIn(max = 800.dp)
                .fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier.weight(1.5f, fill = false),
            ) {
                before()
            }
            Box(
                modifier = Modifier.weight(2.5f, fill = false),
            ) {
                after()
            }
        }
    }
}

@Composable
private fun SectionHeadingText(text: String, modifier: Modifier = Modifier) = Text(
    text = text,
    style = MaterialTheme.typography.headlineMedium,
    fontWeight = FontWeight.Bold,
    modifier = modifier
        .padding(top = 32.dp, bottom = 16.dp),
)

@Composable
private fun BeforeAfterCodeSection(
    textColor: Color,
    backgroundColor: Color,
    label: String,
    code: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier,
    ) {
        Text(
            text = label,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(backgroundColor, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 12.dp),
        )

        Box(
            modifier = Modifier
                .background(backgroundColor.copy(alpha = 0.25f))
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            KotlinCodeBlock(
                code = code,
            )
        }

        Box(
            modifier = Modifier
                .border(4.dp, backgroundColor.copy(alpha = 0.25f))
                .padding(4.dp)
                .fillMaxWidth(),
        ) {
            DisableSelection {
                content()
            }
        }
    }
}

internal object CustomizedInfoTab : InspectorTab {
    override val title: String = "About"
    override val icon: @Composable (() -> Painter) = { painterResource(Res.drawable.icon_add_notes) }
    override val content: @Composable ((state: PreviewLabState) -> Unit) = { _ ->
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "✨ Custom Tab Feature",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "This is a custom inspector tab!",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "You can create your own tabs by implementing the InspectorTab interface:",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                ) {
                    KotlinCodeBlock(
                        code = """
                        object MyTab : InspectorTab {
                          override val title: String = "My Tab"
                          override val icon: @Composable () -> Painter = { ... }
                          override val content: @Composable (PreviewLabState) -> Unit = { ... }
                        }
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        contentPadding = PaddingValues(12.dp),
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Use cases:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                listOf(
                    "📖 Component documentation",
                    "💡 Usage examples & tips",
                    "🎨 Design guidelines",
                    "🐛 Debug information",
                    "📊 Performance metrics",
                    "♿ Accessibility info",
                ).forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun NextActionSection() {
    SectionHeadingText(
        text = "Next Steps",
    )

    Text(
        text = "Now that you understand the basics of Compose Preview Lab, here's what you can explore next:",
        style = MaterialTheme.typography.bodyMedium,
    )

    Spacer(Modifier.height(16.dp))

    val previewLabGalleryNavigator = LocalPreviewLabGalleryNavigator.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                previewLabGalleryNavigator.navigateOr("AboutFields") { }
            },
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Learn About Fields",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Discover how to dynamically control preview parameters with built-in and custom Fields",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@ComposePreviewLabOption(displayName = "1: About Compose Preview Lab", id = "AboutComposePreviewLab")
@Preview
@Composable
private fun AboutComposePreviewLabPreview() = Column {
    AboutComposePreviewLab()
}
