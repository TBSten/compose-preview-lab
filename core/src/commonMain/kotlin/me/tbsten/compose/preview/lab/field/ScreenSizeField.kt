package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.component.SelectButton
import me.tbsten.compose.preview.lab.field.ScreenSize.Companion.MediumSmartPhone
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * Field to specify the screen size.
 * Preview を表示する画面のサイズを指定します。
 *
 * @param label Field Label.
 * @param sizes Instantiate ScreenSize or specify a convenient preset in ScreenSize.Companion.
 * @param type Select UI type, default is [Type.DROPDOWN]. See also [SelectableField.Type].
 */
open class ScreenSizeField(
    label: String = "ScreenSize",
    private val sizes: List<ScreenSize> = listOf(MediumSmartPhone),
    type: Type = Type.DROPDOWN,
    initialValue: ScreenSize = sizes[0],
) : SelectableField<ScreenSize>(
    label = label,
    choices = sizes,
    choiceLabel = { it.label },
    type = type,
    initialValue = initialValue,
) {
    @Composable
    override fun Content() {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SelectButton(
                value = value,
                choices = choices,
                onSelect = { value = it },
                title = {
                    if (it in choices) {
                        it.label
                    } else {
                        "Custom"
                    }
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextFieldContent(
                    toString = { it.width.value.toString() },
                    toValue = {
                        runCatching {
                            ScreenSize(
                                width = it.toFloat().dp,
                                height = value.height,
                            )
                        }
                    },
                    placeholder = { Text("width") },
                    modifier = Modifier.weight(1f),
                )

                TextFieldContent(
                    toString = { it.height.value.toString() },
                    toValue = {
                        runCatching {
                            ScreenSize(
                                width = value.width,
                                height = it.toFloat().dp,
                            )
                        }
                    },
                    placeholder = { Text("height") },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * ScreenSize class to represent the size of a screen in Dp.
 *
 * @param width Width of the screen in Dp.
 * @param height Height of the screen in Dp.
 * @param label A string that is displayed as a choice in the selection UI. If there is a special name (e.g., SmallSmartPhone, LargeDesktop), it may be specified for clarity.
 *
 * @see me.tbsten.compose.preview.lab.field.ScreenSize.Companion
 */
class ScreenSize(val width: Dp, val height: Dp, val label: String = "${width}x$height") {
    /**
     * Returns the ScreenSize with width and height replaced.
     */
    fun reversed() = ScreenSize(
        label = label,
        width = height,
        height = width,
    )

    /**
     * 一般的なデバイスのサイズの Preset が定義されています。
     */
    companion object {
        // Smartphones
        val SmallSmartPhone = ScreenSize(
            label = "Small Smartphone (320x568)",
            width = 320.dp,
            height = 568.dp,
        )
        val MediumSmartPhone = ScreenSize(
            label = "Medium Smartphone (375x667)",
            width = 375.dp,
            height = 667.dp,
        )
        val LargeSmartPhone = ScreenSize(
            label = "Large Smartphone (430x926)",
            width = 430.dp,
            height = 926.dp,
        )

        val PortraitSmartPhones = listOf(
            SmallSmartPhone,
            MediumSmartPhone,
            LargeSmartPhone,
        )

        val LandscapeSmartPhones = PortraitSmartPhones.map {
            it.reversed()
        }

        val SmartPhones = PortraitSmartPhones + LandscapeSmartPhones

        // Tablets
        val SmallTablet = ScreenSize(
            label = "Small Tablet (600x1024)",
            width = 600.dp,
            height = 1024.dp,
        )
        val MediumTablet = ScreenSize(
            label = "Medium Tablet (768x1366)",
            width = 768.dp,
            height = 1366.dp,
        )
        val LargeTablet = ScreenSize(
            label = "Large Tablet (900x1440)",
            width = 900.dp,
            height = 1440.dp,
        )

        val PortraitTablets = listOf(
            SmallTablet,
            MediumTablet,
            LargeTablet,
        )

        val LandscapeTablets = PortraitTablets.map {
            it.reversed()
        }

        val Tablets = PortraitTablets + LandscapeTablets

        // Desktop PCs
        val SmallDesktop = ScreenSize(
            label = "Small Desktop (1024x768)",
            width = 1024.dp,
            height = 768.dp,
        )
        val MediumDesktop = ScreenSize(
            label = "Medium Desktop (1440x900)",
            width = 1440.dp,
            height = 900.dp,
        )
        val LargeDesktop = ScreenSize(
            label = "Large Desktop (1920x1080)",
            width = 1920.dp,
            height = 1080.dp,
        )

        val Desktops = listOf(
            SmallDesktop,
            MediumDesktop,
            LargeDesktop,
        )

        val SmartphoneAndDesktops = SmartPhones + Desktops
        val AllPresets = SmartPhones + Tablets + Desktops
    }
}
