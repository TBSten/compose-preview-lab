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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import compose_preview_lab_integration_test.hellocomposepreviewlab.generated.resources.icon_arrow_right_alt
import compose_preview_lab_integration_test.hellocomposepreviewlab.generated.resources.icon_check
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.ColorField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.gallery.LocalPreviewLabGalleryNavigator
import me.tbsten.compose.preview.lab.gallery.navigateOr
import me.tbsten.compose.preview.lab.previewlab.openfilehandler.LocalOpenFileHandler
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab
import me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component.DocPage
import me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component.IconBox
import me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component.KotlinCodeBlock
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// FIXME migrate LabDoc API

@Composable
internal fun AboutComposePreviewLab() {
    DocPage {
        CoverSection()

        QuickSummarySection()

        BeforeAfterSection()

        NextActionSection()
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
    modifier = Modifier
        .fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
) {
    val cover by Res.drawable.cover.preloadImageVector()
    val uriHandler = LocalUriHandler.current
    val githubUrl = "https://github.com/TBSten/compose-preview-lab"

    val coverImageTransition =
        SingletonStore.stored { MutableTransitionState<ImageBitmap?>(null) }
            .apply { targetState = cover }

    Card(
        modifier = Modifier
            .widthIn(max = 700.dp)
            .fillMaxWidth(),
    ) {
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
                        .aspectRatio(391f / 220)
                        .fillMaxWidth(),
                )
            } else {
                Box(
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .aspectRatio(391f / 220)
                        .fillMaxWidth(),
                )
            }
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "GitHub:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = githubUrl,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { uriHandler.openUri(githubUrl) },
        )
    }
}

@Composable
internal expect fun DrawableResource.preloadImageVector(): State<ImageBitmap?>

@Composable
private fun QuickSummarySection() = Column(
    verticalArrangement = Arrangement.spacedBy(16.dp),
) {
    SectionHeadingText(
        text = "Quick Summary",
        iconBox = { IconBox(color = MaterialTheme.colorScheme.primary, icon = painterResource(Res.drawable.icon_check)) },
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FeatureBullet(
                icon = { IconBox(color = MaterialTheme.colorScheme.secondary, label = "P") },
                text = "Collect and display @Preview!",
            )
            FeatureBullet(
                icon = { IconBox(color = MaterialTheme.colorScheme.tertiary, label = "G") },
                text = "Turn @Preview into a powerful Playground with just a little code!",
            )
        }
    }
}

@Composable
private fun FeatureBullet(icon: @Composable () -> Unit, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, InternalComposePreviewLabApi::class)
@Composable
private fun BeforeAfterSection() = Column(
    verticalArrangement = Arrangement.spacedBy(16.dp),
) {
    SectionHeadingText(
        text = "What is Compose Preview Lab ?",
        iconBox = { IconBox(color = MaterialTheme.colorScheme.secondary, label = "?") },
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
                labelColor = MaterialTheme.colorScheme.onErrorContainer,
                labelBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                codeBackgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                contentBorderColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
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
                labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                labelBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                codeBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                contentBorderColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                label = "after",
                code = """
                    @Preview
                    @Composable
                    private fun MyButtonPreview() = PreviewLab(
                        inspectorTabs = InspectorTab.defaults + listOf(MyCustomTab()) // ← Custom tab!
                    ) {
                        MyButton(
                            text = fieldValue { StringField("text", "Click Me !") },
                            isEnable = fieldValue { BooleanField("isEnable", true) },
                            onClick = { onEvent("onClick") },
                        )
                    }
                """.trimIndent(),
                content = {
                    CompositionLocalProvider(
                        LocalPreviewLabGalleryNavigator provides null,
                        LocalOpenFileHandler provides null,
                    ) {
                        PreviewLab(
                            isHeaderShow = false,
                            inspectorTabs = InspectorTab.defaults + listOf(CustomizedInfoTab),
                            modifier = Modifier.height(450.dp),
                        ) {
                            val defaultButtonColor = MaterialTheme.colorScheme.primary
                            Button(
                                enabled = fieldValue { BooleanField("isEnable", true) },
                                onClick = { onEvent("onClick") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = fieldValue { ColorField("colors.containerColor", defaultButtonColor) },
                                ),
                            ) {
                                Text(text = fieldValue { StringField("text", "Click Me !") })
                            }
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
                .fillMaxWidth(),
        ) {
            before()
            after()
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .widthIn(max = 1000.dp)
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
private fun SectionHeadingText(text: String, modifier: Modifier = Modifier, iconBox: (@Composable () -> Unit)? = null) {
    if (iconBox != null) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier,
        ) {
            iconBox()
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = modifier
                .padding(top = 32.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun BeforeAfterCodeSection(
    labelColor: Color,
    labelBackgroundColor: Color,
    codeBackgroundColor: Color,
    contentBorderColor: Color,
    label: String,
    code: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                text = label,
                color = labelColor,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(labelBackgroundColor)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
            )

            Box(
                modifier = Modifier
                    .background(codeBackgroundColor)
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                KotlinCodeBlock(
                    code = code,
                )
            }

            Box(
                modifier = Modifier
                    .border(4.dp, contentBorderColor)
                    .padding(4.dp)
                    .fillMaxWidth(),
            ) {
                content()
            }
        }
    }
}

internal object CustomizedInfoTab : InspectorTab {
    override val title: String = "About"
    override val icon: @Composable (() -> Painter) = { painterResource(Res.drawable.icon_add_notes) }

    @Composable
    override fun InspectorTab.ContentContext.Content() {
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
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
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
private fun NextActionSection() = Column(
    verticalArrangement = Arrangement.spacedBy(16.dp),
) {
    SectionHeadingText(
        text = "Next Steps",
        iconBox = {
            IconBox(
                color = MaterialTheme.colorScheme.primary,
                icon = painterResource(Res.drawable.icon_arrow_right_alt),
            )
        },
    )

    Text(
        text = "Now that you understand the basics of Compose Preview Lab, here's what you can explore next:",
        style = MaterialTheme.typography.bodyLarge,
    )

    val previewLabGalleryNavigator = LocalPreviewLabGalleryNavigator.current

    Card(
        onClick = { previewLabGalleryNavigator.navigateOr("AboutFields") { } },
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Learn About Fields",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Discover how to dynamically control preview parameters with built-in and custom Fields",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconBox(
                color = MaterialTheme.colorScheme.primaryContainer,
                icon = painterResource(Res.drawable.icon_arrow_right_alt),
            )
        }
    }

    Card(
        onClick = { previewLabGalleryNavigator.navigateOr("AboutEvents") { } },
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Learn About Events",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Record and visualize user interactions in your Previews without complex testing infrastructure",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconBox(
                color = MaterialTheme.colorScheme.primaryContainer,
                icon = painterResource(Res.drawable.icon_arrow_right_alt),
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
