package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString

class AnimateContentSizeModifierFieldValue : ModifierFieldValue {
    override fun Modifier.createModifier(): Modifier = animateContentSize(
        // TODO animationSpec
        // TODO finishedListener
    )

    @Composable
    override fun Builder() = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            append(".animateContentSize()")
        },
        menuContent = null,
    )

    class Factory : ModifierFieldValueFactory<AnimateContentSizeModifierFieldValue> {
        override val title: String = ".animateContentSize(...)"

        override val canCreate: Boolean = true

        @Composable
        override fun Content(createButton: @Composable (() -> Unit)) {
        }

        override fun create(): Result<AnimateContentSizeModifierFieldValue> = runCatching {
            AnimateContentSizeModifierFieldValue()
        }
    }
}

fun ModifierFieldValueList.animateContentSize() = then(
    AnimateContentSizeModifierFieldValue(),
)
