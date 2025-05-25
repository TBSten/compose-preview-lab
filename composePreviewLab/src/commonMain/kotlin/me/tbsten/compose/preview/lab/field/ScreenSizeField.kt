package me.tbsten.compose.preview.lab.field

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.field.ScreenSize.Companion.FitContent

open class ScreenSizeField(
    label: String = "ScreenSize",
    sizes: List<ScreenSize?> = listOf(FitContent),
    initialValue: ScreenSize? = sizes[0],
) : SelectableField<ScreenSize?>(
    label = label,
    choices = sizes,
    choiceLabel = { it?.label ?: "Fit Content" },
    initialValue = initialValue,
)

class ScreenSize(
    val width: Dp?,
    val height: Dp?,
    val label: String = "${width}x${height}",
) {
    fun reversed() = ScreenSize(
        label = label,
        width = height,
        height = width,
    )

    companion object {
        val FitContent: ScreenSize? = null

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
