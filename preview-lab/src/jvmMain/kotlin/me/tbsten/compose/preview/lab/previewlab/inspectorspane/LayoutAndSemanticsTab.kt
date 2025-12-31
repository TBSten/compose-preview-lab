@file:OptIn(ExperimentalComposeUiApi::class)

package me.tbsten.compose.preview.lab.previewlab.inspectorspane

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.previewlab.LocalComposeWindow
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.CommonIconButton
import me.tbsten.compose.preview.lab.ui.components.Divider
import me.tbsten.compose.preview.lab.ui.components.Switch
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_refresh
import org.jetbrains.compose.resources.painterResource

@ExperimentalComposePreviewLabApi
data object LayoutAndSemanticsTab : InspectorTab {
    override val title: String = "Layout and Semantics"

    @Composable
    override fun InspectorTab.ContentContext.Content() = SelectionContainer {
        var useUnmerged by remember { mutableStateOf(false) }

        val semanticsOwner =
            LocalComposeWindow.current
                ?.semanticsOwners
                ?.firstOrNull()
                ?: run {
                    Text("Not supported.")
                    return@SelectionContainer
                }

        var _previewLabContent by remember(semanticsOwner, useUnmerged) {
            mutableStateOf(getContentSemanticsNodeTree(semanticsOwner, useUnmerged))
        }.also { state ->
            LaunchedEffect(state) {
                while (true) {
                    state.value = getContentSemanticsNodeTree(semanticsOwner, useUnmerged)
                    delay(0.2.seconds)
                }
            }
        }
        val previewLabContent = _previewLabContent ?: run {
            Text("Not supported: Not found PreviewLab.content")
            return@SelectionContainer
        }

        LazyColumn {
            stickyHeader {
                Header(
                    onRefresh = { _previewLabContent = getContentSemanticsNodeTree(semanticsOwner, useUnmerged) },
                )
            }

            item {
                UseUnmergedSwitchRow(
                    useUnmerged = useUnmerged,
                    onChange = { useUnmerged = it },
                )
            }

            item {
                SemanticsNodeView(
                    node = previewLabContent,
                    isRoot = true,
                    currentDeps = 0,
                    maxDeps = 5,
                )
            }
        }
    }
}

private fun getContentSemanticsNodeTree(
    semanticsOwner: SemanticsOwner,
    useUnmerged: Boolean,
): SemanticsNode? {
    val root =
        semanticsOwner
            .run { if (useUnmerged) unmergedRootSemanticsNode else rootSemanticsNode }

    return root
        .find { it.config.getOrNull(SemanticsProperties.TestTag) == "PreviewLab.content" }
}

fun SemanticsNode.find(includeSelf: Boolean = true, block: (SemanticsNode) -> Boolean): SemanticsNode? {
    if (includeSelf && block(this)) return this
    return this.children.firstNotNullOfOrNull { it.find(true, block) }
}

@Composable
private fun Header(
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Spacer(Modifier.weight(1f))
        CommonIconButton(
            painter = painterResource(PreviewLabUiRes.drawable.icon_refresh),
            contentDescription = "Refresh",
            onClick = onRefresh,
        )
    }
}

@Composable
private fun UseUnmergedSwitchRow(
    useUnmerged: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable { onChange(!useUnmerged) }
            .padding(8.dp)
            .fillMaxWidth(),
    ) {
        Switch(
            checked = useUnmerged,
            onCheckedChange = onChange,
        )

        Text("use unmerged", style = PreviewLabTheme.typography.label1)
    }
}

@Composable
private fun SemanticsNodeView(
    node: SemanticsNode,
    isRoot: Boolean,
    currentDeps: Int,
    maxDeps: Int,
) {
    if (maxDeps < currentDeps) return

    val isRoot = isRoot || node.isRoot

    Column {
        Row {
            val text: String =
                "(Root)".takeIf { isRoot }
                // Fallback
                // config.Text
                    ?: node.config.getOrNull(SemanticsProperties.Text)?.joinToString(", ")
                    // config.InputText
                    ?: node.config.getOrNull(SemanticsProperties.InputText)?.toString()
                    // (config.TestTag)
                    ?: node.config.getOrNull(SemanticsProperties.TestTag)?.let { "(TestTag=${it})" }
                    // (config.Role)
                    ?: node.config.getOrNull(SemanticsProperties.Role)?.let { "(Role=${it})" }
                    // (first 30 char of config.ContentDescription)
                    ?: node.config.getOrNull(SemanticsProperties.ContentDescription)
                        ?.joinToString(", ")
                        ?.let { cd -> "${cd.take(30)}${"...".takeIf { cd.length > 30 }}" }
                        ?.let { "(ContentDescription=${it})" }
                    // top,left widthxheight
                    ?: node.boundsInRoot.let { "size=${it.top},${it.left} ${it.width}x${it.height}" }

            Text(text)
        }

        Divider(Modifier.padding(vertical = 12.dp))

        Column(Modifier.padding(start = 20.dp)) {
            node.children.forEach { child ->

                SemanticsNodeView(
                    node = child,
                    isRoot = false,
                    currentDeps = currentDeps + 1,
                    maxDeps = maxDeps,
                )
            }
        }
    }
}
