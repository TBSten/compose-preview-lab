package me.tbsten.compose.preview.lab.field.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.adaptive
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButtonVariant
import me.tbsten.compose.preview.lab.ui.components.PreviewLabCommonIconButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabDivider
import me.tbsten.compose.preview.lab.ui.components.PreviewLabModal
import me.tbsten.compose.preview.lab.ui.components.PreviewLabSurface
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.components.card.PreviewLabCardDefaults
import me.tbsten.compose.preview.lab.ui.components.card.PreviewLabOutlinedCard
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_add
import org.jetbrains.compose.resources.painterResource

@Composable
@InternalComposePreviewLabApi
internal fun <Value> CollectionFieldEditModal(
    label: String,
    fields: MutableList<MutablePreviewLabField<Value>>,
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onInsertAt: (Int) -> Unit,
    onAfterDelete: () -> Unit = {},
    isDuplicate: (MutablePreviewLabField<Value>) -> Boolean = { false },
    modifier: Modifier = Modifier,
) {
    PreviewLabModal(
        isVisible = isVisible,
        contentAlignment = Alignment.TopCenter,
        onDismissRequest = onDismissRequest,
    ) {
        SelectionContainer {
            PreviewLabSurface(
                color = PreviewLabTheme.colors.background,
                contentColor = PreviewLabTheme.colors.onBackground,
                modifier = modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    PreviewLabText(
                        text = "Edit $label (${fields.size})",
                        style = PreviewLabTheme.typography.h2,
                    )

                    CollectionFieldElementsEditor(
                        fields = fields,
                        onDelete = {
                            fields.remove(it)
                            onAfterDelete()
                        },
                        onInsertAt = onInsertAt,
                        isDuplicate = isDuplicate,
                    )
                }
            }
        }
    }
}

@Composable
private fun <Value> CollectionFieldElementsEditor(
    fields: List<MutablePreviewLabField<Value>>,
    onDelete: (MutablePreviewLabField<Value>) -> Unit,
    onInsertAt: (Int) -> Unit,
    isDuplicate: (MutablePreviewLabField<Value>) -> Boolean = { false },
    modifier: Modifier = Modifier,
) {
    var selectedField by remember { mutableStateOf<MutablePreviewLabField<Value>?>(null) }
    val duplicateValues = remember(fields, isDuplicate) {
        fields.filter { isDuplicate(it) }.map { it.valueCode() }.distinct()
    }

    Column(modifier) {
        if (duplicateValues.isNotEmpty()) {
            PreviewLabText(
                text = "Duplicate values detected: ${duplicateValues.joinToString(", ")}",
                style = PreviewLabTheme.typography.body2,
                color = PreviewLabTheme.colors.error,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        CollectionFieldElementsRow(
            fields = fields,
            selectedField = selectedField,
            onFieldSelect = { field ->
                selectedField = field.takeIf { field != selectedField }
            },
            onInsertAt = {
                onInsertAt(it)
                selectedField = fields.getOrNull(it)
            },
            isDuplicate = isDuplicate,
        )

        PreviewLabDivider(color = PreviewLabTheme.colors.primary, thickness = 1.5.dp)

        SelectedFieldView(
            selectedField = selectedField,
            onDelete = {
                selectedField?.let { onDelete(it) }
                selectedField = null
            },
        )
    }
}

@Composable
private fun <Value> CollectionFieldElementsRow(
    fields: List<MutablePreviewLabField<Value>>,
    selectedField: MutablePreviewLabField<Value>?,
    onFieldSelect: (MutablePreviewLabField<Value>) -> Unit,
    onInsertAt: (Int) -> Unit,
    isDuplicate: (MutablePreviewLabField<Value>) -> Boolean = { false },
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        verticalAlignment = Alignment.Top,
        flingBehavior = rememberSnapFlingBehavior(listState),
        modifier = modifier,
    ) {
        if (fields.isEmpty()) {
            // Show only insert button when collection is empty
            item {
                Column {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.height(100.dp),
                    ) {
                        InsertButton(onClick = { onInsertAt(0) })
                    }
                    Spacer(Modifier.height(12.dp + 8.dp))
                }
            }
        } else {
            items(fields.size, key = { fields[it] }) { index ->
                val elementField = fields[index]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.animateItem(),
                ) {
                    Column {
                        InsertButton(onClick = { onInsertAt(index) })
                        Spacer(Modifier.height(12.dp + 8.dp))
                    }
                    Column {
                        CollectionElementCard(
                            isSelected = elementField == selectedField,
                            isError = isDuplicate(elementField),
                            onClick = { onFieldSelect(elementField) },
                            displayText = elementField.valueCode(),
                        )
                        SelectedArrow(
                            isSelected = selectedField == elementField,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                    // Also place insert button on the right side of the last element
                    if (index == fields.lastIndex) {
                        Column {
                            InsertButton(onClick = { onInsertAt(fields.size) })
                            Spacer(Modifier.height(12.dp + 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsertButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    PreviewLabCommonIconButton(
        painter = painterResource(PreviewLabUiRes.drawable.icon_add),
        contentDescription = "Insert",
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun SelectedArrow(isSelected: Boolean, modifier: Modifier = Modifier) {
    val arrowColor by animateColorAsState(if (isSelected) PreviewLabTheme.colors.primary else Color.Transparent)

    Canvas(modifier.padding(top = 8.dp).width(20.dp).height(12.dp)) {
        drawPath(
            Path().apply {
                moveTo(size.width / 2, 0f)
                lineTo(0f, size.height)
                lineTo(size.width, size.height)
                close()
            },
            color = arrowColor,
        )
    }
}

@Composable
private fun <Value> SelectedFieldView(
    selectedField: MutablePreviewLabField<Value>?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = selectedField,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        modifier = modifier,
    ) { field ->
        val spacing = adaptive(12.dp, 20.dp)

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier.fillMaxSize().padding(spacing),
        ) {
            if (field == null) {
                PreviewLabText("No selected", style = PreviewLabTheme.typography.body2)
            } else {
                FlowRow {
                    PreviewLabButton(
                        variant = PreviewLabButtonVariant.Destructive,
                        onClick = onDelete,
                    ) {
                        PreviewLabText("Delete ${field.valueCode().takeEllipsis(10)}")
                    }
                }
                field.Content()
            }
        }
    }
}

private fun String.takeEllipsis(max: Int) = if (length < max) this else (this.take(max - 3) + "...")

@Composable
private fun CollectionElementCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    displayText: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = when {
        isError -> PreviewLabCardDefaults.cardColors(
            containerColor = PreviewLabTheme.colors.error,
            contentColor = PreviewLabTheme.colors.onError,
        )
        isSelected -> PreviewLabCardDefaults.cardColors()
        else -> PreviewLabCardDefaults.outlinedCardColors()
    }
    PreviewLabOutlinedCard(
        colors = colors,
        onClick = onClick,
        border = PreviewLabCardDefaults.outlinedCardBorder(
            borderWidth = 2.dp,
            color = if (isSelected) PreviewLabTheme.colors.primary else PreviewLabTheme.colors.outline,
        ),
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .widthIn(
                    min = 100.dp,
                    max = 180.dp,
                ).height(100.dp)
                .padding(12.dp),
        ) {
            PreviewLabText(
                text = displayText,
                style = PreviewLabTheme.typography.body3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
