package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.spatial.RelativeLayoutBounds
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.field.modifier.CaptureOffsetModifierFieldValue.Type
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

// captureSize

class CaptureSizeModifierFieldValue : ModifierFieldValue {
    var capturedSize by mutableStateOf<IntSize?>(null)

    override fun Modifier.createModifier(): Modifier = onSizeChanged {
        capturedSize = it
    }

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            append(".onSizeChanged { ... }")
        },
        footer = {
            capturedSize?.let { capturedSize ->
                Column {
                    val (width, height) =
                        with(LocalDensity.current) {
                            capturedSize.width.toDp().value to capturedSize.height.toDp().value
                        }

                    Text(
                        text = buildAnnotatedString {
                            append("    → width: $width.dp")
                            appendLine()
                            append("    → height: $height.dp")
                        },
                        style = PreviewLabTheme.typography.body3,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
    )

    class Factory : ModifierFieldValueFactory<CaptureSizeModifierFieldValue> {
        override val title: String = ".onSizeChanged { /* capture size */ }"
        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { }

        override fun create(): Result<CaptureSizeModifierFieldValue> = runCatching {
            CaptureSizeModifierFieldValue()
        }
    }
}

fun ModifierFieldValueList.captureSize() = then(
    CaptureSizeModifierFieldValue(),
)

// captureOffset

class CaptureOffsetModifierFieldValue(initialCaptureType: Type = Type.PositionInRoot) : ModifierFieldValue {
    var capturedOffset by mutableStateOf<LayoutCoordinates?>(null)
    var captureType by mutableStateOf<Type>(initialCaptureType)

    override fun Modifier.createModifier(): Modifier = onPlaced {
        capturedOffset = it
    }

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            append(".onPlaced { ... }")
        },
        footer = {
            capturedOffset?.let { capturedOffset ->
                Column {
                    val (x, y) =
                        with(LocalDensity.current) {
                            val getOffset = captureType.getOffset
                            getOffset(capturedOffset).x.toDp().value to
                                getOffset.invoke(capturedOffset).y.toDp().value
                        }

                    Text(
                        text = buildAnnotatedString {
                            append("    → x: $x.dp")
                            appendLine()
                            append("    → height: $y.dp")
                        },
                        style = PreviewLabTheme.typography.body3,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        menuContent = {
            DefaultMenu {
                SelectItem(
                    label = "capture type",
                    value = captureType,
                    onValueChange = { captureType = it },
                    choices = Type.entries,
                    title = Type::label,
                )
            }
        },
    )

    enum class Type(val label: String, val getOffset: LayoutCoordinates.() -> Offset) {
        PositionInRoot("positionInRoot", { positionInRoot() }),
        PositionOnScreen("positionOnScreen", { positionOnScreen() }),
        PositionInWindow("positionInWindow", { positionInWindow() }),
        PositionInParent("positionInParent", { positionInParent() }),
    }

    class Factory : ModifierFieldValueFactory<CaptureOffsetModifierFieldValue> {
        override val title: String = ".onPlaced { /* capture offset */ }"
        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { }

        override fun create(): Result<CaptureOffsetModifierFieldValue> = runCatching {
            CaptureOffsetModifierFieldValue()
        }
    }
}

fun ModifierFieldValueList.captureOffset(captureType: Type = Type.PositionInRoot) = then(
    CaptureOffsetModifierFieldValue(
        initialCaptureType = captureType,
    ),
)

// captureLayoutRect

class CaptureLayoutRectModifierFieldValue(initialCaptureType: Type = Type.PositionInRoot) : ModifierFieldValue {
    var capturedLayoutRect by mutableStateOf<RelativeLayoutBounds?>(null)
    var captureType by mutableStateOf<Type>(initialCaptureType)

    override fun Modifier.createModifier(): Modifier = onLayoutRectChanged {
        capturedLayoutRect = it
    }

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            append(".onLayoutRectChanged { ... }")
        },
        footer = {
            capturedLayoutRect?.let { capturedLayoutRect ->
                Column {
                    with(LocalDensity.current) {
                        val (x, y, width, height) = run {
                            val getLayoutRect = captureType.getLayoutRect
                            getLayoutRect.invoke(capturedLayoutRect)
                        }

                        Text(
                            text = buildAnnotatedString {
                                append("    → x: ${x.toDp().value}.dp")
                                appendLine()
                                append("    → y: ${y.toDp().value}.dp")
                                appendLine()
                                append("    → width: ${width.toDp().value}.dp")
                                appendLine()
                                append("    → height: ${height.toDp().value}.dp")
                            },
                            style = PreviewLabTheme.typography.body3,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        },
        menuContent = {
            DefaultMenu {
                SelectItem(
                    label = "capture type",
                    value = captureType,
                    onValueChange = { captureType = it },
                    choices = Type.entries,
                    title = Type::label,
                )
            }
        },
    )

    enum class Type(val label: String, val getLayoutRect: RelativeLayoutBounds.() -> IntRect) {
        PositionInRoot("positionInRoot", { boundsInRoot }),
        PositionInScreen("positionInScreen", { boundsInScreen }),
        BoundsInWindow("boundsInWindow", { boundsInWindow }),
    }

    class Factory : ModifierFieldValueFactory<CaptureLayoutRectModifierFieldValue> {
        override val title: String = ".onLayoutRectChanged { /* capture offset and size */ }"
        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { }

        override fun create(): Result<CaptureLayoutRectModifierFieldValue> = runCatching {
            CaptureLayoutRectModifierFieldValue()
        }
    }
}

fun ModifierFieldValueList.captureLayoutRect(
    captureType: CaptureLayoutRectModifierFieldValue.Type = CaptureLayoutRectModifierFieldValue.Type.PositionInRoot,
) = then(
    CaptureLayoutRectModifierFieldValue(
        initialCaptureType = captureType,
    ),
)
