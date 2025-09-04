package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString

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

fun ModifierFieldValueList.wrapContentSize() = then(
    WrapContentSizeModifierFieldValue(),
)

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

fun ModifierFieldValueList.wrapContentWidth() = then(
    WrapContentWidthModifierFieldValue(),
)

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

    class Factory : ModifierFieldValueFactory<WrapContentHeightModifierFieldValue> {
        override val title: String = ".wrapContentHeight(...)"

        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) {
        }

        override fun create(): Result<WrapContentHeightModifierFieldValue> = runCatching {
            WrapContentHeightModifierFieldValue()
        }
    }
}

fun ModifierFieldValueList.wrapContentHeight() = then(
    WrapContentHeightModifierFieldValue(),
)
