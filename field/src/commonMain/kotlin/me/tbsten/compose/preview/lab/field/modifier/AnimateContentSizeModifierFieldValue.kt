package me.tbsten.compose.preview.lab.field.modifier

import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString

/**
 * ModifierFieldValue that animates content size changes with smooth transitions.
 *
 * When the content size changes, this modifier provides smooth animation transitions
 * instead of abrupt size changes.
 */
public class AnimateContentSizeModifierFieldValue : ModifierFieldValue {
    override fun Modifier.createModifier(): Modifier = animateContentSize(
        // TODO animationSpec
        // TODO finishedListener
    )

    @Composable
    override fun Builder(): Unit = DefaultModifierFieldValueBuilder(
        modifierTextCode = buildAnnotatedString {
            append(".animateContentSize()")
        },
        menuContent = null,
    )

    /**
     * Factory for creating AnimateContentSizeModifierFieldValue instances.
     * No initial configuration parameters are needed as it automatically animates content size changes.
     */
    public class Factory : ModifierFieldValueFactory<AnimateContentSizeModifierFieldValue> {
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

/**
 * Animates content size changes with smooth transitions.
 *
 * @return A new ModifierFieldValueList with animated content size applied
 */
public fun ModifierFieldValueList.animateContentSize(): ModifierFieldValueList = then(
    AnimateContentSizeModifierFieldValue(),
)
