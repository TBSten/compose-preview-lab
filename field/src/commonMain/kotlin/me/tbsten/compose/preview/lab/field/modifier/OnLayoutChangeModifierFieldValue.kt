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

/**
 * A ModifierFieldValue that captures layout size changes
 *
 * This class monitors size changes of Compose components and captures
 * size information (width, height) when changes occur. It's useful for
 * debugging and layout adjustment, allowing real-time size information
 * verification. Values are displayed in dp units with automatic density conversion.
 *
 * ```kotlin
 * // Basic usage
 * modifier = ModifierFieldValueList().captureSize()
 *
 * // Direct creation
 * val captureSizeField = CaptureSizeModifierFieldValue()
 *
 * // Usage in components
 * Box(modifier = modifier.apply { createModifier() }) {
 *     Text("Hello World")
 * }
 * ```
 *
 * @see ModifierFieldValue
 * @see captureSize
 */
public class CaptureSizeModifierFieldValue : ModifierFieldValue {
    public var capturedSize: IntSize? by mutableStateOf<IntSize?>(null)

    override fun Modifier.createModifier(): Modifier = onSizeChanged {
        capturedSize = it
    }

    @Composable
    override fun Builder(): Unit = DefaultModifierFieldValueBuilder(
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

    /**
     * Factory class for creating CaptureSizeModifierFieldValue instances
     *
     * Implements ModifierFieldValueFactory interface to handle
     * creation button display and instance generation in the UI.
     */
    public class Factory : ModifierFieldValueFactory<CaptureSizeModifierFieldValue> {
        override val title: String = ".onSizeChanged { /* capture size */ }"
        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)): Unit = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { }

        override fun create(): Result<CaptureSizeModifierFieldValue> = runCatching {
            CaptureSizeModifierFieldValue()
        }
    }
}

/**
 * Extension function to add size capture functionality to ModifierFieldValueList
 *
 * ```kotlin
 * modifier = ModifierFieldValueList().captureSize()
 * ```
 *
 * @return ModifierFieldValueList with CaptureSizeModifierFieldValue added
 * @see CaptureSizeModifierFieldValue
 */
public fun ModifierFieldValueList.captureSize(): ModifierFieldValueList = then(
    CaptureSizeModifierFieldValue(),
)

// captureOffset

/**
 * A ModifierFieldValue that captures layout offset changes
 *
 * This class monitors position changes of Compose components and captures
 * position information (x, y coordinates) when changes occur.
 * Multiple coordinate systems (Root, Screen, Window, Parent) are supported
 * for position retrieval, used to accurately determine component positions
 * during debugging and layout adjustment.
 *
 * ```kotlin
 * // Offset capture in Root coordinate system
 * modifier = ModifierFieldValueList().captureOffset()
 *
 * // Offset capture in Screen coordinate system
 * modifier = ModifierFieldValueList().captureOffset(Type.PositionOnScreen)
 *
 * // Direct creation
 * val captureOffsetField = CaptureOffsetModifierFieldValue(Type.PositionInWindow)
 * ```
 *
 * @param initialCaptureType Initial coordinate system type (default: PositionInRoot)
 * @see ModifierFieldValue
 * @see captureOffset
 */
public class CaptureOffsetModifierFieldValue(initialCaptureType: Type = Type.PositionInRoot) : ModifierFieldValue {
    public var capturedOffset: LayoutCoordinates? by mutableStateOf<LayoutCoordinates?>(null)
    public var captureType: Type by mutableStateOf<Type>(initialCaptureType)

    override fun Modifier.createModifier(): Modifier = onPlaced {
        capturedOffset = it
    }

    @Composable
    override fun Builder(): Unit = DefaultModifierFieldValueBuilder(
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

    /**
     * Defines coordinate system types for offset capture
     *
     * @param label Label for UI display
     * @param getOffset Lambda to retrieve corresponding Offset from LayoutCoordinates
     */
    public enum class Type(public val label: String, public val getOffset: LayoutCoordinates.() -> Offset) {
        PositionInRoot("positionInRoot", { positionInRoot() }),
        PositionOnScreen("positionOnScreen", { positionOnScreen() }),
        PositionInWindow("positionInWindow", { positionInWindow() }),
        PositionInParent("positionInParent", { positionInParent() }),
    }

    /**
     * Factory class for creating CaptureOffsetModifierFieldValue instances
     *
     * Implements ModifierFieldValueFactory interface to handle
     * creation button display and instance generation in the UI.
     */
    public class Factory : ModifierFieldValueFactory<CaptureOffsetModifierFieldValue> {
        override val title: String = ".onPlaced { /* capture offset */ }"
        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)): Unit = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { }

        override fun create(): Result<CaptureOffsetModifierFieldValue> = runCatching {
            CaptureOffsetModifierFieldValue()
        }
    }
}

/**
 * Extension function to add offset capture functionality to ModifierFieldValueList
 *
 * ```kotlin
 * // Default (Root coordinate system) offset capture
 * modifier = ModifierFieldValueList().captureOffset()
 *
 * // Screen coordinate system offset capture
 * modifier = ModifierFieldValueList().captureOffset(CaptureOffsetModifierFieldValue.Type.PositionOnScreen)
 * ```
 *
 * @param captureType Coordinate system type to use (default: PositionInRoot)
 * @return ModifierFieldValueList with CaptureOffsetModifierFieldValue added
 * @see CaptureOffsetModifierFieldValue
 */
public fun ModifierFieldValueList.captureOffset(captureType: Type = Type.PositionInRoot): ModifierFieldValueList = then(
    CaptureOffsetModifierFieldValue(
        initialCaptureType = captureType,
    ),
)

// captureLayoutRect

/**
 * A ModifierFieldValue that captures layout rectangle (position and size) changes
 *
 * This class monitors layout rectangle changes of Compose components and captures
 * both position information (x, y coordinates) and size information (width, height)
 * when changes occur. Rectangle information retrieval from multiple coordinate systems
 * is supported, useful when more detailed layout information is needed.
 *
 * ```kotlin
 * // Rectangle information capture in Root coordinate system
 * modifier = ModifierFieldValueList().captureLayoutRect()
 *
 * // Rectangle information capture in Screen coordinate system
 * modifier = ModifierFieldValueList().captureLayoutRect(CaptureLayoutRectModifierFieldValue.Type.PositionInScreen)
 *
 * // Direct creation
 * val captureLayoutRectField = CaptureLayoutRectModifierFieldValue(CaptureLayoutRectModifierFieldValue.Type.BoundsInWindow)
 * ```
 *
 * @param initialCaptureType Initial coordinate system type (default: PositionInRoot)
 * @see ModifierFieldValue
 * @see captureLayoutRect
 */
public class CaptureLayoutRectModifierFieldValue(initialCaptureType: Type = Type.PositionInRoot) : ModifierFieldValue {
    public var capturedLayoutRect: RelativeLayoutBounds? by mutableStateOf<RelativeLayoutBounds?>(null)
    public var captureType: Type by mutableStateOf<Type>(initialCaptureType)

    override fun Modifier.createModifier(): Modifier = onLayoutRectChanged {
        capturedLayoutRect = it
    }

    @Composable
    override fun Builder(): Unit = DefaultModifierFieldValueBuilder(
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

    /**
     * Defines coordinate system types for layout rectangle capture
     *
     * @param label Label for UI display
     * @param getLayoutRect Lambda to retrieve corresponding IntRect from RelativeLayoutBounds
     */
    public enum class Type(public val label: String, public val getLayoutRect: RelativeLayoutBounds.() -> IntRect) {
        PositionInRoot("positionInRoot", { boundsInRoot }),
        PositionInScreen("positionInScreen", { boundsInScreen }),
        BoundsInWindow("boundsInWindow", { boundsInWindow }),
    }

    /**
     * Factory class for creating CaptureLayoutRectModifierFieldValue instances
     *
     * Implements ModifierFieldValueFactory interface to handle
     * creation button display and instance generation in the UI.
     */
    public class Factory : ModifierFieldValueFactory<CaptureLayoutRectModifierFieldValue> {
        override val title: String = ".onLayoutRectChanged { /* capture offset and size */ }"
        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)): Unit = Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { }

        override fun create(): Result<CaptureLayoutRectModifierFieldValue> = runCatching {
            CaptureLayoutRectModifierFieldValue()
        }
    }
}

/**
 * Extension function to add layout rectangle capture functionality to ModifierFieldValueList
 *
 * ```kotlin
 * // Default (Root coordinate system) rectangle information capture
 * modifier = ModifierFieldValueList().captureLayoutRect()
 *
 * // Screen coordinate system rectangle information capture
 * modifier = ModifierFieldValueList().captureLayoutRect(CaptureLayoutRectModifierFieldValue.Type.PositionInScreen)
 * ```
 *
 * @param captureType Coordinate system type to use (default: PositionInRoot)
 * @return ModifierFieldValueList with CaptureLayoutRectModifierFieldValue added
 * @see CaptureLayoutRectModifierFieldValue
 */
public fun ModifierFieldValueList.captureLayoutRect(
    captureType: CaptureLayoutRectModifierFieldValue.Type = CaptureLayoutRectModifierFieldValue.Type.PositionInRoot,
): ModifierFieldValueList = then(
    CaptureLayoutRectModifierFieldValue(
        initialCaptureType = captureType,
    ),
)
