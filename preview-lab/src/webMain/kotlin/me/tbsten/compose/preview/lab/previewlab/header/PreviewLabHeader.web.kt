package me.tbsten.compose.preview.lab.previewlab.header

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import me.tbsten.compose.preview.lab.LocalPreviewLabPreview
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Button
import me.tbsten.compose.preview.lab.ui.components.ButtonDefaults
import me.tbsten.compose.preview.lab.ui.components.ButtonVariant
import me.tbsten.compose.preview.lab.ui.components.Checkbox
import me.tbsten.compose.preview.lab.ui.components.Icon
import me.tbsten.compose.preview.lab.ui.components.SelectButton
import me.tbsten.compose.preview.lab.ui.components.SimpleModal
import me.tbsten.compose.preview.lab.ui.components.Surface
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.textfield.OutlinedTextField
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_arrow_drop_down
import org.jetbrains.compose.resources.painterResource

private val json = Json {
    prettyPrint = false
}

/**
 * コピーURL用のパラメータ項目。
 */
private sealed class CopyParam(val key: String, val displayLabel: String) {
    /** シリアライズ可能かどうか */
    abstract val canSerialize: Boolean

    /** シリアライズした値を返す */
    abstract fun serialize(): String?

    /** previewIdパラメータ */
    class PreviewId(private val id: String?) :
        CopyParam(
            key = "previewId",
            displayLabel = "Preview ID",
        ) {
        override val canSerialize: Boolean = id != null
        override fun serialize(): String? = id
    }

    /** フィールドパラメータ */
    class Field(private val field: PreviewLabField<*>) :
        CopyParam(
            key = field.label,
            displayLabel = field.label,
        ) {
        @Suppress("UNCHECKED_CAST")
        private val serializer: KSerializer<in Any?>? = field.serializer() as? KSerializer<in Any?>?

        override val canSerialize: Boolean = serializer != null

        override fun serialize(): String? = serializer?.let {
            json.encodeToString(it, field.value)
        }
    }

    class State<T>(key: String, displayLabel: String, private val serializer: KSerializer<T>, private val value: T) :
        CopyParam(
            key = key,
            displayLabel = displayLabel,
        ) {
        override val canSerialize: Boolean = true
        override fun serialize(): String = json.encodeToString(serializer, value)
    }
}

/**
 * コピーURL用のパラメータMapを構築する。
 *
 * @param params コピー対象のパラメータリスト
 * @param selected 各パラメータが選択されているかのMap（nullの場合は全て選択）
 */
private fun buildCopyParams(params: List<CopyParam>, selected: Map<CopyParam, Boolean>? = null): Map<String, String> =
    buildMap {
        params.forEach { param ->
            val isSelected = selected?.get(param) ?: true
            if (isSelected && param.canSerialize) {
                param.serialize()?.let { put(param.key, it) }
            }
        }
    }

@Composable
internal actual fun RowScope.PlatformHeaders(state: PreviewLabState) {
    Spacer(Modifier.weight(1f))

    val previewIdParam = CopyParam.PreviewId(LocalPreviewLabPreview.current?.id)
    val copyUrl = copyUserHandler(buildCopyParams(listOf(previewIdParam)))
    var isDetailDialogOpen by remember { mutableStateOf(false) }
    fun closeDetailDialog() {
        isDetailDialogOpen = false
    }

    Row(
        modifier = Modifier.align(Alignment.CenterVertically),
    ) {
        Button(
            text = "Copy",
            variant = ButtonVariant.PrimaryOutlined,
            onClick = { copyUrl() },
            shape = ButtonDefaults.ButtonShape.copy(topEnd = CornerSize(0), bottomEnd = CornerSize(0)),
        )

        Button(
            variant = ButtonVariant.PrimaryOutlined,
            onClick = { isDetailDialogOpen = !isDetailDialogOpen },
            shape = ButtonDefaults.ButtonShape.copy(topStart = CornerSize(0), bottomStart = CornerSize(0)),
            contentPadding = PaddingValues(
                horizontal = ButtonDefaults.contentPadding.calculateTopPadding(),
                vertical = ButtonDefaults.contentPadding.calculateTopPadding(),
            ),
        ) {
            Icon(
                painter = painterResource(PreviewLabUiRes.drawable.icon_arrow_drop_down),
                modifier = Modifier.size(16.dp),
            )
        }
    }

    DisableSelection {
        SimpleModal(
            isVisible = isDetailDialogOpen,
            onDismissRequest = ::closeDetailDialog,
        ) {
            SelectionContainer {
                Surface(
                    color = PreviewLabTheme.colors.background,
                    contentColor = PreviewLabTheme.colors.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    ) {
                        Text(
                            text = "Copy setting",
                            style = PreviewLabTheme.typography.h1,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        )

                        var selectType by remember { mutableStateOf(CopyType.OnlyId) }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        ) {
                            SelectButton(
                                value = selectType,
                                choices = CopyType.entries,
                                onSelect = { selectType = it },
                                title = { it.title },
                            )

                            AnimatedContent(
                                targetState = selectType,
                                transitionSpec = {
                                    (fadeIn() + expandVertically(expandFrom = Alignment.Top)) togetherWith
                                        (fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)) using
                                        SizeTransform(clip = false)
                                },
                            ) { selectType ->
                                when (selectType) {
                                    CopyType.OnlyId -> OnlyIdContent(::closeDetailDialog)
                                    CopyType.WithDetails -> WithDetailsContent(state, ::closeDetailDialog)
                                    CopyType.Full -> FullContent(state, ::closeDetailDialog)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class CopyType(val title: String) {
    OnlyId(title = "Copy only the ID"),
    WithDetails(title = "Select include details"),
    Full(title = "Include all details"),
}

@Composable
private fun OnlyIdContent(close: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        val copy = copyUserHandler(buildCopyParams(listOf(CopyParam.PreviewId(LocalPreviewLabPreview.current?.id))))

        Column(Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = copy.link,
                    onValueChange = { },
                    enabled = true,
                    maxLines = 3,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    text = "Copy",
                    onClick = {
                        copy()
                        close()
                    },
                )
            }
        }
    }
}

@Composable
private fun WithDetailsContent(state: PreviewLabState, close: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        val previewId = LocalPreviewLabPreview.current?.id
        val params = remember(previewId, state.fields) {
            listOf(CopyParam.PreviewId(previewId)) +
                state.fields.map { CopyParam.Field(it) } +
                CopyParam.State("contentScale", "Content scale", Float.serializer(), state.contentScale)
        }
        val selected = remember(params) {
            mutableStateMapOf(*params.associateWith { true }.entries.map { it.key to it.value }.toTypedArray())
        }
        // selected の各エントリを明示的に読み取ることで変更を追跡
        val copyParams by remember(params) {
            derivedStateOf {
                buildMap {
                    params.forEach { param ->
                        val isSelected = selected[param] ?: true
                        if (isSelected && param.canSerialize) {
                            param.serialize()?.let { put(param.key, it) }
                        }
                    }
                }
            }
        }
        val copy = copyUserHandler(copyParams)

        params.forEach { param ->
            val textStyle = PreviewLabTheme.typography.body1
                .copy(color = if (param.canSerialize) PreviewLabTheme.colors.text else PreviewLabTheme.colors.onDisabled)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = selected[param] ?: false,
                    enabled = param.canSerialize,
                    onCheckedChange = { selected[param] = !selected.getValue(param) },
                )
                Spacer(Modifier.width(4.dp))

                Text(
                    text = param.displayLabel,
                    style = textStyle,
                    modifier = Modifier.widthIn(min = 100.dp),
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = param.serialize() ?: "(Can not include)",
                    style = textStyle,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Column(Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = copy.link,
                    onValueChange = { },
                    enabled = true,
                    maxLines = 3,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    text = "Copy",
                    onClick = {
                        copy()
                        close()
                    },
                )
            }
        }
    }
}

@Composable
private fun FullContent(state: PreviewLabState, close: () -> Unit) {
    val previewId = LocalPreviewLabPreview.current?.id
    val params = remember(previewId, state.fields) {
        listOf(CopyParam.PreviewId(previewId)) +
            state.fields.map { CopyParam.Field(it) } +
            CopyParam.State("contentScale", "Content scale", Float.serializer(), state.contentScale)
    }
    val copy = copyUserHandler(buildCopyParams(params))

    Column(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = copy.link,
                onValueChange = { },
                enabled = true,
                maxLines = 3,
                modifier = Modifier.weight(1f),
            )
            Button(
                text = "Copy",
                onClick = {
                    copy()
                    close()
                },
            )
        }
    }
}

@Composable
internal expect fun copyUserHandler(params: Map<String, String>): CopyUserHandler

internal expect class CopyUserHandler {
    val link: String

    operator fun invoke()
}
