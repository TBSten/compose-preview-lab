package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString

/**
 * ModifierFieldValue that wraps the content size (both width and height) to match the content bounds.
 */
class WrapContentSizeModifierFieldValue : ModifierFieldValue {
    override fun Modifier.createModifier(): Modifier = wrapContentSize(
        // TODO align
        // TODO unbounded
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            append(".wrapContentSize()")
        },
        menuContent = null,
    )

    /**
     * Factory for creating WrapContentSizeModifierFieldValue instances.
     * No initial configuration parameters are needed as it automatically wraps to content size.
     */
    class Factory : ModifierFieldValueFactory<WrapContentSizeModifierFieldValue> {
        override val title: String = ".wrapContentSize(...)"

        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) {
        }

        override fun create(): Result<WrapContentSizeModifierFieldValue> = runCatching {
            WrapContentSizeModifierFieldValue()
        }
    }
}

/**
 * Wraps the content size (both width and height) to match the content bounds.
 *
 * @return A new ModifierFieldValueList with wrap content size applied
 */
fun ModifierFieldValueList.wrapContentSize() = then(
    WrapContentSizeModifierFieldValue(),
)

/**
 * ModifierFieldValue that wraps the content width to match the content bounds.
 */
class WrapContentWidthModifierFieldValue : ModifierFieldValue {
    override fun Modifier.createModifier(): Modifier = wrapContentWidth(
        // TODO align
        // TODO unbounded
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            append(".wrapContentWidth()")
        },
        menuContent = null,
    )

    /**
     * Factory for creating WrapContentWidthModifierFieldValue instances.
     * No initial configuration parameters are needed as it automatically wraps to content width.
     */
    class Factory : ModifierFieldValueFactory<WrapContentWidthModifierFieldValue> {
        override val title: String = ".wrapContentWidth(...)"

        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) {
        }

        override fun create(): Result<WrapContentWidthModifierFieldValue> = runCatching {
            WrapContentWidthModifierFieldValue()
        }
    }
}

/**
 * Wraps the content width to match the content bounds.
 *
 * @return A new ModifierFieldValueList with wrap content width applied
 */
fun ModifierFieldValueList.wrapContentWidth() = then(
    WrapContentWidthModifierFieldValue(),
)

/**
 * ModifierFieldValue that wraps the content height to match the content bounds.
 */
class WrapContentHeightModifierFieldValue : ModifierFieldValue {
    override fun Modifier.createModifier(): Modifier = wrapContentHeight(
        // TODO align
        // TODO unbounded
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            append(".wrapContentHeight()")
        },
        menuContent = null,
    )

    /**
     * Factory for creating WrapContentHeightModifierFieldValue instances.
     * No initial configuration parameters are needed as it automatically wraps to content height.
     */
    class Factory : ModifierFieldValueFactory<WrapContentHeightModifierFieldValue> {
        override val title: String = ".wrapContentHeight(...)"

        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) {
            // No configuration needed for wrapContentHeight
        }

        override fun create(): Result<WrapContentHeightModifierFieldValue> = runCatching {
            WrapContentHeightModifierFieldValue()
        }
    }
}

/**
 * Wraps the content height to match the content bounds.
 *
 * @return A new ModifierFieldValueList with wrap content height applied
 */
fun ModifierFieldValueList.wrapContentHeight() = then(
    WrapContentHeightModifierFieldValue(),
)
