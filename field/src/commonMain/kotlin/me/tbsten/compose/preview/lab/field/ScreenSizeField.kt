package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.serialization.KSerializer
import me.tbsten.compose.preview.lab.field.ScreenSize.Companion.MediumSmartPhone
import me.tbsten.compose.preview.lab.field.serializer.ScreenSizeSerializer
import me.tbsten.compose.preview.lab.field.component.TextFieldContent
import me.tbsten.compose.preview.lab.ui.components.SelectButton
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * A field for selecting screen sizes in the preview environment
 *
 * Provides a user interface for selecting from predefined screen sizes or creating custom dimensions.
 * The field displays as a dropdown with common device sizes and allows manual width/height adjustment.
 * Essential for testing responsive designs across different device form factors.
 *
 * ```kotlin
 * // Basic usage with default smartphone sizes
 * val screenSize = fieldValue { ScreenSizeField() }
 *
 * // Custom sizes with specific presets
 * val customScreenSize = fieldValue {
 *     ScreenSizeField(
 *         label = "Device Size",
 *         sizes = ScreenSize.SmartPhones + ScreenSize.Tablets
 *     )
 * }
 *
 * // With specific initial size
 * val tabletSize = fieldValue {
 *     ScreenSizeField(
 *         initialValue = ScreenSize.LargeTablet,
 *         sizes = ScreenSize.Tablets
 *     )
 * }
 * ```
 *
 * @param label Display label for the field
 * @param sizes List of available screen size options, defaults to medium smartphone
 * @param type UI presentation type, defaults to dropdown selection
 * @param initialValue Starting screen size selection
 * @see ScreenSize
 * @see SelectableField.Type
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
    override fun testValues(): List<ScreenSize> = super.testValues() + sizes
    override fun serializer(): KSerializer<ScreenSize> = ScreenSizeSerializer

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
 * Represents a screen size with width and height in density-independent pixels
 *
 * Encapsulates screen dimensions for different device types, providing common presets
 * for smartphones, tablets, and desktop displays. Used primarily in ScreenSizeField
 * for responsive design testing and preview environments.
 *
 * ```kotlin
 * // Create custom screen size
 * val customSize = ScreenSize(width = 400.dp, height = 800.dp, label = "Custom Phone")
 *
 * // Use predefined presets
 * val phone = ScreenSize.MediumSmartPhone
 * val tablet = ScreenSize.LargeTablet
 * val desktop = ScreenSize.MediumDesktop
 *
 * // Create landscape version
 * val landscapePhone = ScreenSize.MediumSmartPhone.reversed()
 * ```
 *
 * @param width Screen width in dp
 * @param height Screen height in dp
 * @param label Display name for UI selection (auto-generated if not provided)
 * @see ScreenSizeField
 * @see ScreenSize.Companion
 */
class ScreenSize(val width: Dp, val height: Dp, val label: String = "${width}x$height") {
    /**
     * Creates a landscape orientation of this screen size
     *
     * Swaps width and height dimensions to convert between portrait and landscape orientations.
     * Useful for testing how layouts adapt to device rotation.
     *
     * ```kotlin
     * val portrait = ScreenSize(375.dp, 667.dp, "iPhone")
     * val landscape = portrait.reversed() // 667.dp x 375.dp
     * ```
     *
     * @return New ScreenSize with swapped dimensions
     */
    fun reversed() = ScreenSize(
        label = label,
        width = height,
        height = width,
    )

    /**
     * Common device size presets for testing responsive designs
     *
     * Provides predefined screen sizes for smartphones, tablets, and desktop displays
     * in both portrait and landscape orientations. These presets represent typical
     * device dimensions found in the market and help ensure compatibility across
     * different screen sizes.
     *
     * ```kotlin
     * // Individual device types
     * ScreenSize.MediumSmartPhone  // 375x667 dp
     * ScreenSize.LargeTablet       // 900x1440 dp
     * ScreenSize.MediumDesktop     // 1440x900 dp
     *
     * // Device categories
     * ScreenSize.SmartPhones       // All smartphone sizes
     * ScreenSize.Tablets          // All tablet sizes
     * ScreenSize.Desktops         // All desktop sizes
     * ScreenSize.AllPresets       // Every available preset
     * ```
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
