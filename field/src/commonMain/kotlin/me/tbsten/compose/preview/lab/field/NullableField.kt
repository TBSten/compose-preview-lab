package me.tbsten.compose.preview.lab.field

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.ui.components.Checkbox
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * Create a PreviewLabField that makes the receiver's PreviewLabField nullable.
 *
 * # Usage
 *
 * ```kt
 * // Basic nullable field
 * @Preview
 * @Composable
 * fun UserNamePreview() = PreviewLab {
 *     val userName: String? = fieldValue {
 *         StringField("User Name", "John Doe")
 *             .nullable()
 *     }
 *     UserName(userName = userName)
 * }
 *
 * // Nullable with IntField for optional configuration
 * @Preview
 * @Composable
 * fun TimeoutPreview() = PreviewLab {
 *     val timeout: Int? = fieldValue {
 *         IntField("Timeout (seconds)", 30)
 *             .nullable(initialValue = null)
 *     }
 *     MyComponent(timeout = timeout) // null means no timeout
 * }
 * ```
 *
 * @see NullableField
 */
fun <Value : Any> PreviewLabField<Value>.nullable(initialValue: Value? = this.initialValue) = NullableField(
    baseField = this,
    initialValue = initialValue,
)

/**
 * Create a PreviewLabField that makes the receiver's PreviewLabField nullable.
 *
 * # Usage
 *
 * ```kt
 * // Basic nullable string field
 * @Preview
 * @Composable
 * fun AvatarPreview() = PreviewLab {
 *     val avatarUrl: String? = fieldValue {
 *         StringField("Avatar URL", "https://example.com/avatar.jpg")
 *             .nullable(initialValue = null)
 *     }
 *     UserProfile(avatarUrl = avatarUrl) // Show placeholder if null
 * }
 *
 * // Combining nullable with EnumField
 * enum class IconType { HOME, SEARCH, SETTINGS }
 * @Preview
 * @Composable
 * fun IconPreview() = PreviewLab {
 *
 *     val icon: IconType? = fieldValue {
 *         EnumField("Icon", IconType.HOME)
 *             .nullable()
 *     }
 *     MyButton(icon = icon) // Button without icon if null
 * }
 * ```
 *
 * @see nullable
 */
class NullableField<Value : Any> internal constructor(private val baseField: PreviewLabField<Value>, initialValue: Value?) :
    MutablePreviewLabField<Value?>(
        label = baseField.label,
        initialValue = initialValue,
    ) {
    override fun testValues(): List<Value?> = super.testValues() + listOf(null) + baseField.testValues()

    private var isNull by mutableStateOf(initialValue == null)
    override var value: Value?
        get() = if (isNull) null else baseField.value
        set(newValue) {
            if (newValue == null) {
                isNull = true
            } else {
                isNull = false
                if (baseField is MutablePreviewLabField) {
                    baseField.value = newValue
                }
            }
        }

    override fun valueCode(): String = if (value == null) "null" else baseField.valueCode()

    override fun serializer(): KSerializer<Value?>? = baseField.serializer()?.nullable

    @Composable
    override fun Content() {
//        val baseFieldContent = remember { movableContentOf { baseField.Content() } }

        AnimatedContent(
            targetState = isNull,
            transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) { isNull ->
            if (!isNull) {
                Row {
                    SwitchIsNullCheckbox()
                    baseField.Content()
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SwitchIsNullCheckbox()
                    Text(
                        text = "null",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    @Composable
    private fun SwitchIsNullCheckbox() {
        Checkbox(
            checked = !isNull,
            onCheckedChange = { this@NullableField.isNull = !isNull },
        )
    }
}
