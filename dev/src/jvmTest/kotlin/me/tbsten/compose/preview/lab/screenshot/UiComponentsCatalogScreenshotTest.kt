@file:OptIn(
    ExperimentalTestApi::class,
    UiComposePreviewLabApi::class,
    ExperimentalLayoutApi::class,
)

package me.tbsten.compose.preview.lab.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.dp
import io.kotest.core.spec.style.StringSpec
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButtonSize
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButtonStyle
import me.tbsten.compose.preview.lab.ui.components.PreviewLabCheckbox
import me.tbsten.compose.preview.lab.ui.components.PreviewLabChip
import me.tbsten.compose.preview.lab.ui.components.PreviewLabDivider
import me.tbsten.compose.preview.lab.ui.components.PreviewLabHorizontalSeparator
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIcon
import me.tbsten.compose.preview.lab.ui.components.PreviewLabIconButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabListHeader
import me.tbsten.compose.preview.lab.ui.components.PreviewLabListItem
import me.tbsten.compose.preview.lab.ui.components.PreviewLabMenu
import me.tbsten.compose.preview.lab.ui.components.PreviewLabModal
import me.tbsten.compose.preview.lab.ui.components.PreviewLabRadio
import me.tbsten.compose.preview.lab.ui.components.PreviewLabRadioGroup
import me.tbsten.compose.preview.lab.ui.components.PreviewLabSelectButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabSwitch
import me.tbsten.compose.preview.lab.ui.components.PreviewLabTab
import me.tbsten.compose.preview.lab.ui.components.PreviewLabTabList
import me.tbsten.compose.preview.lab.ui.components.PreviewLabTabPanel
import me.tbsten.compose.preview.lab.ui.components.PreviewLabTabs
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText
import me.tbsten.compose.preview.lab.ui.components.PreviewLabTooltipBox
import me.tbsten.compose.preview.lab.ui.components.PreviewLabTriStateCheckbox
import me.tbsten.compose.preview.lab.ui.components.PreviewLabVerticalSeparator
import me.tbsten.compose.preview.lab.ui.components.card.PreviewLabCard
import me.tbsten.compose.preview.lab.ui.components.card.PreviewLabOutlinedCard
import me.tbsten.compose.preview.lab.ui.components.colorpicker.PreviewLabColorPicker
import me.tbsten.compose.preview.lab.ui.components.textfield.PreviewLabTextField
import me.tbsten.compose.preview.lab.ui.components.textfield.PreviewLabTextFieldStyle
import me.tbsten.compose.preview.lab.ui.components.toast.ToastAction
import me.tbsten.compose.preview.lab.ui.components.toast.ToastDuration
import me.tbsten.compose.preview.lab.ui.components.toast.ToastHost
import me.tbsten.compose.preview.lab.ui.components.toast.ToastType
import me.tbsten.compose.preview.lab.ui.components.toast.rememberToastHostState
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_check
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_code
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_delete
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_edit
import org.jetbrains.compose.resources.painterResource

/**
 * Catalog screenshots of the internal UI kit (`me.tbsten.compose.preview.lab.ui.*`) as rendered by
 * the CURRENT implementation (Composables UI components on compose-unstyled). This is the one
 * harness file that intentionally depends on the `ui` module API.
 *
 * Each component group is one self-contained `@Composable` catalog below, rendered once in the
 * light theme and once in the dark theme (`60-component-<Name>-light.png` / `-dark.png`). The file
 * names are the same as in the pre-migration (Lumo UI) catalog so BEFORE / AFTER images pair up 1:1;
 * the sections show the new API axes (button style x size, text field styles, radio group, tabs).
 *
 * Same selection rules as [UiScreenshotTest]: tagged [Screenshot], skipped unless
 * `previewlab.screenshot.dir` is set.
 */
class UiComponentsCatalogScreenshotTest :
    StringSpec({

        componentCase("Button", height = 1000) { ButtonCatalog() }
        componentCase("IconButton") { IconButtonCatalog() }
        componentCase("SelectionControls", height = 760) { SelectionControlsCatalog() }
        componentCase("Chip") { ChipCatalog() }
        componentCase("TextField", height = 1300) { TextFieldCatalog() }
        componentCase("SelectButton") { SelectButtonCatalog() }
        componentCase(
            name = "SelectButton-opened",
            height = 420,
            captureWholeWindow = true,
            interact = { onNodeWithText("Option B").performClick() },
        ) { SelectButtonCatalog() }
        componentCase("Menu", height = 420, captureWholeWindow = true) { MenuCatalog() }
        componentCase("Modal", height = 520, captureWholeWindow = true) { ModalCatalog() }
        componentCase(
            name = "Tooltip",
            height = 320,
            captureWholeWindow = true,
            // The tooltip opens on hover (hoverDelayMillis = 0); there is no forced-visible state any more.
            interact = { onNodeWithText("Anchor").performMouseInput { moveTo(center) } },
        ) { TooltipCatalog() }
        componentCase("TabRow") { TabRowCatalog() }
        componentCase("Card") { CardCatalog() }
        componentCase("Divider") { DividerCatalog() }
        componentCase("Toast", height = 700) { ToastCatalog() }
        componentCase("ColorPicker") { ColorPickerCatalog() }
        componentCase("List") { ListCatalog() }
        componentCase("Text", height = 900) { TextCatalog() }
    })

// ---------------------------------------------------------------------- runner

private const val CatalogTag = "screenshot-component-catalog"
private const val CatalogGroup = "components-before"
private const val CatalogWindowWidth = 720
private val CatalogWidth = 680.dp

/**
 * Registers two screenshot cases (light + dark) for one component catalog.
 *
 * @param captureWholeWindow capture the full window instead of cropping to the catalog node;
 *   required when a Popup / Dialog / tooltip (menu, modal, tooltip, opened select button) is shown,
 *   because those layers extend beyond the catalog's own bounds.
 * @param interact performed after the first `awaitIdle()` (e.g. open a dropdown, hover an anchor).
 */
private fun StringSpec.componentCase(
    name: String,
    height: Int = 640,
    captureWholeWindow: Boolean = false,
    interact: suspend ComposeUiTest.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    listOf(false, true).forEach { isDarkTheme ->
        val themeName = if (isDarkTheme) "dark" else "light"
        val fileName = "60-component-$name-$themeName.png"
        val title = "UI kit component: $name ($themeName theme)"
        screenshotCase(title = title, group = CatalogGroup, fileName = fileName) {
            runDesktopComposeUiTest(width = CatalogWindowWidth, height = height) {
                setContent { CatalogFrame(isDarkTheme = isDarkTheme, content = content) }
                awaitIdle()
                interact(this)
                awaitIdle()
                val image = if (captureWholeWindow) {
                    captureWindow()
                } else {
                    onNodeWithTag(CatalogTag, useUnmergedTree = true).captureToImage()
                }
                ScreenshotOutput.save(
                    image = image,
                    fileName = fileName,
                    title = title,
                    group = CatalogGroup,
                    note = if (captureWholeWindow) "whole ${CatalogWindowWidth}x$height window (popup/dialog open)" else null,
                )
            }
        }
    }
}

/** Applies the ui-module theme and lays the catalog out in a 680dp wide column on the theme background. */
@Composable
private fun CatalogFrame(isDarkTheme: Boolean, content: @Composable ColumnScope.() -> Unit) {
    PreviewLabTheme(isDarkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PreviewLabTheme.colors.background),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .testTag(CatalogTag)
                    .width(CatalogWidth)
                    .background(PreviewLabTheme.colors.background)
                    .padding(16.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PreviewLabText(
            text = title,
            style = PreviewLabTheme.typography.label2,
            color = PreviewLabTheme.colors.textSecondary,
        )
        content()
    }
}

@Composable
private fun WrapRow(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun Labeled(label: String, content: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        content()
        PreviewLabText(label, style = PreviewLabTheme.typography.body3)
    }
}

// ---------------------------------------------------------------------- catalogs

private val ButtonStyles = listOf(
    "Primary" to PreviewLabButtonStyle.Primary,
    "Secondary" to PreviewLabButtonStyle.Secondary,
    "Outlined" to PreviewLabButtonStyle.Outlined,
    "Destructive" to PreviewLabButtonStyle.Destructive,
    "Ghost" to PreviewLabButtonStyle.Ghost,
    "Link" to PreviewLabButtonStyle.Link,
)

private val ButtonSizes = listOf(
    "Small" to PreviewLabButtonSize.Small,
    "Regular" to PreviewLabButtonSize.Regular,
    "Large" to PreviewLabButtonSize.Large,
)

/** PreviewLabButton: every [PreviewLabButtonStyle] x [PreviewLabButtonSize] + disabled / selected / icon content. */
@Composable
private fun ButtonCatalog() {
    Section("PreviewLabButton styles (Regular size)") {
        WrapRow {
            ButtonStyles.forEach { (name, style) ->
                PreviewLabButton(text = name, onClick = {}, style = style)
            }
        }
    }
    Section("PreviewLabButton style x size (Small / Regular / Large)") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ButtonStyles.forEach { (styleName, style) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ButtonSizes.forEach { (sizeName, size) ->
                        PreviewLabButton(text = "$styleName $sizeName", onClick = {}, style = style, buttonSize = size)
                    }
                }
            }
        }
    }
    Section("PreviewLabButton disabled (every style)") {
        WrapRow {
            ButtonStyles.forEach { (name, style) ->
                PreviewLabButton(text = name, onClick = {}, style = style, enabled = false)
            }
        }
    }
    Section("PreviewLabButton selected (every style; honoured by Secondary / Outlined / Ghost)") {
        WrapRow {
            ButtonStyles.forEach { (name, style) ->
                PreviewLabButton(text = name, onClick = {}, style = style, selected = true)
            }
        }
    }
    Section("PreviewLabButton states") {
        WrapRow {
            PreviewLabButton(text = "Disabled", onClick = {}, enabled = false)
            PreviewLabButton(text = "Selected", onClick = {}, style = PreviewLabButtonStyle.Secondary, selected = true)
            PreviewLabButton(text = "Selected ghost", onClick = {}, style = PreviewLabButtonStyle.Ghost, selected = true)
            PreviewLabButton(
                text = "Outlined disabled",
                onClick = {},
                style = PreviewLabButtonStyle.Outlined,
                enabled = false,
            )
            PreviewLabButton(onClick = {}, style = PreviewLabButtonStyle.Outlined) {
                PreviewLabIcon(painter = painterResource(PreviewLabUiRes.drawable.icon_code), contentDescription = null)
                Spacer(Modifier.width(8.dp))
                PreviewLabText("Source Code")
            }
        }
    }
}

/** PreviewLabIconButton: every [PreviewLabButtonStyle] and [PreviewLabButtonSize] + disabled / content / tinted. */
@Composable
private fun IconButtonCatalog() {
    Section("PreviewLabIconButton styles (painter overload, Small, with tooltip box)") {
        WrapRow {
            ButtonStyles.forEach { (name, style) ->
                Labeled(name) {
                    PreviewLabIconButton(
                        painter = painterResource(PreviewLabUiRes.drawable.icon_edit),
                        contentDescription = name,
                        onClick = {},
                        style = style,
                    )
                }
            }
        }
    }
    Section("PreviewLabIconButton sizes (Outlined)") {
        WrapRow {
            ButtonSizes.forEach { (name, size) ->
                Labeled(name) {
                    PreviewLabIconButton(
                        painter = painterResource(PreviewLabUiRes.drawable.icon_edit),
                        contentDescription = name,
                        onClick = {},
                        style = PreviewLabButtonStyle.Outlined,
                        buttonSize = size,
                    )
                }
            }
        }
    }
    Section("PreviewLabIconButton states") {
        WrapRow {
            Labeled("disabled") {
                PreviewLabIconButton(
                    painter = painterResource(PreviewLabUiRes.drawable.icon_edit),
                    contentDescription = "disabled",
                    onClick = {},
                    enabled = false,
                    style = PreviewLabButtonStyle.Primary,
                )
            }
            Labeled("content overload (Primary)") {
                PreviewLabIconButton(
                    onClick = {},
                    style = PreviewLabButtonStyle.Primary,
                ) {
                    PreviewLabIcon(
                        painter = painterResource(PreviewLabUiRes.drawable.icon_edit),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Labeled("tinted") {
                PreviewLabIconButton(
                    painter = painterResource(PreviewLabUiRes.drawable.icon_delete),
                    contentDescription = "delete",
                    onClick = {},
                    tint = PreviewLabTheme.colors.error,
                )
            }
        }
    }
}

/** PreviewLabCheckbox / PreviewLabTriStateCheckbox / PreviewLabSwitch / PreviewLabRadioGroup + PreviewLabRadio. */
@Composable
private fun SelectionControlsCatalog() {
    Section("PreviewLabCheckbox") {
        WrapRow {
            Labeled("checked") { PreviewLabCheckbox(checked = true, onCheckedChange = {}) }
            Labeled("unchecked") { PreviewLabCheckbox(checked = false, onCheckedChange = {}) }
            Labeled("checked, disabled") { PreviewLabCheckbox(checked = true, onCheckedChange = {}, enabled = false) }
            Labeled("unchecked, disabled") { PreviewLabCheckbox(checked = false, onCheckedChange = {}, enabled = false) }
            PreviewLabCheckbox(checked = true, onCheckedChange = {}) { PreviewLabText("with content label") }
        }
    }
    Section("PreviewLabTriStateCheckbox") {
        WrapRow {
            Labeled("indeterminate") {
                PreviewLabTriStateCheckbox(state = ToggleableState.Indeterminate, onStateChange = {})
            }
            Labeled("on") { PreviewLabTriStateCheckbox(state = ToggleableState.On, onStateChange = {}) }
            Labeled("off") { PreviewLabTriStateCheckbox(state = ToggleableState.Off, onStateChange = {}) }
            Labeled("indeterminate, disabled") {
                PreviewLabTriStateCheckbox(state = ToggleableState.Indeterminate, onStateChange = {}, enabled = false)
            }
        }
    }
    Section("PreviewLabSwitch") {
        WrapRow {
            Labeled("on") { PreviewLabSwitch(checked = true, onCheckedChange = {}) }
            Labeled("off") { PreviewLabSwitch(checked = false, onCheckedChange = {}) }
            Labeled("on, disabled") { PreviewLabSwitch(checked = true, onCheckedChange = {}, enabled = false) }
            Labeled("off, disabled") { PreviewLabSwitch(checked = false, onCheckedChange = {}, enabled = false) }
        }
    }
    Section("PreviewLabRadioGroup + PreviewLabRadio (value = \"a\")") {
        PreviewLabRadioGroup(value = "a", onValueChange = {}) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PreviewLabRadio("a") { PreviewLabText("selected") }
                PreviewLabRadio("b") { PreviewLabText("unselected") }
                PreviewLabRadio("c", enabled = false) { PreviewLabText("unselected, disabled") }
            }
        }
    }
    Section("PreviewLabRadioGroup (selected item disabled)") {
        PreviewLabRadioGroup(value = "a", onValueChange = {}) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PreviewLabRadio("a", enabled = false) { PreviewLabText("selected, disabled") }
                PreviewLabRadio("b") { PreviewLabText("unselected") }
            }
        }
    }
}

/** PreviewLabChip in unselected / selected / disabled / icon states. */
@Composable
private fun ChipCatalog() {
    Section("PreviewLabChip") {
        WrapRow {
            PreviewLabChip(label = { PreviewLabText("Chip") })
            PreviewLabChip(selected = true, label = { PreviewLabText("Selected") })
            PreviewLabChip(enabled = false, label = { PreviewLabText("Disabled") })
            PreviewLabChip(selected = true, enabled = false, label = { PreviewLabText("Selected, disabled") })
            PreviewLabChip(
                leadingIcon = { ChipIcon() },
                label = { PreviewLabText("Leading icon") },
            )
            PreviewLabChip(
                selected = true,
                trailingIcon = { ChipIcon() },
                label = { PreviewLabText("Trailing icon") },
            )
        }
    }
}

@Composable
private fun ChipIcon() {
    PreviewLabIcon(
        painter = painterResource(PreviewLabUiRes.drawable.icon_check),
        contentDescription = null,
        modifier = Modifier.size(16.dp),
    )
}

private val TextFieldStyles = listOf(
    "Filled" to PreviewLabTextFieldStyle.Filled,
    "Outlined" to PreviewLabTextFieldStyle.Outlined,
    "Ghost" to PreviewLabTextFieldStyle.Ghost,
)

/** PreviewLabTextField in every [PreviewLabTextFieldStyle] (Filled / Outlined / Ghost) and common states. */
@Composable
private fun TextFieldCatalog() {
    TextFieldStyles.forEach { (name, style) ->
        Section("PreviewLabTextField(style = $name)") {
            PreviewLabTextField(value = "Hello Compose Preview Lab", onValueChange = {}, style = style)
            PreviewLabTextField(
                value = "",
                onValueChange = {},
                style = style,
                placeholder = { PreviewLabText("Search @Preview") },
            )
            PreviewLabTextField(
                value = "invalid",
                onValueChange = {},
                style = style,
                isError = true,
                supportingText = { PreviewLabText("Error message") },
            )
            PreviewLabTextField(value = "disabled", onValueChange = {}, style = style, enabled = false)
            if (style == PreviewLabTextFieldStyle.Filled) {
                PreviewLabTextField(
                    value = "with decorations",
                    onValueChange = {},
                    style = style,
                    label = { PreviewLabText("Label") },
                    prefix = { PreviewLabText("$") },
                    suffix = { PreviewLabText("dp") },
                    supportingText = { PreviewLabText("Supporting text") },
                )
            } else {
                PreviewLabTextField(
                    value = "16",
                    onValueChange = {},
                    style = style,
                    singleLine = true,
                    suffix = { PreviewLabText("dp") },
                    label = { PreviewLabText("Label") },
                )
            }
        }
    }
}

/** PreviewLabSelectButton (closed; the `-opened` case clicks "Option B" to open its menu). */
@Composable
private fun SelectButtonCatalog() {
    Section("PreviewLabSelectButton") {
        WrapRow {
            PreviewLabSelectButton(
                value = "Option B",
                choices = listOf("Option A", "Option B", "Option C"),
                onSelect = {},
                title = { it },
                itemDetail = { "detail of $it" },
            )
            PreviewLabSelectButton(
                value = "Medium Smartphone (375x667)",
                choices = listOf("Small Smartphone (320x568)", "Medium Smartphone (375x667)", "Large Desktop (1920x1080)"),
                onSelect = {},
                title = { it },
            )
        }
    }
}

/** PreviewLabMenu (always expanded) with PreviewLabListItem entries, anchored to a text. */
@Composable
private fun MenuCatalog() {
    Section("PreviewLabMenu (expanded) + PreviewLabListItem") {
        Box {
            PreviewLabText("menu anchor")
            PreviewLabMenu(expanded = true, onDismissRequest = {}) {
                PreviewLabListItem(title = "Reset Value", isSelected = false, onSelect = {})
                PreviewLabListItem(title = "Type: DROPDOWN", isSelected = false, onSelect = {}, isEnabled = false)
                PreviewLabListItem(title = "Type: CHIPS", isSelected = true, onSelect = {})
                PreviewLabListItem(title = "Type: RADIO", isSelected = false, onSelect = {})
            }
        }
    }
}

/** PreviewLabModal (visible) over some catalog content. */
@Composable
private fun ModalCatalog() {
    Section("PreviewLabModal (visible)") {
        PreviewLabText("Content behind the modal scrim")
        PreviewLabButton(text = "Open modal", onClick = {})
    }
    PreviewLabModal(isVisible = true, onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .background(PreviewLabTheme.colors.panel, RoundedCornerShape(8.dp))
                .padding(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PreviewLabText("Modal title", style = PreviewLabTheme.typography.h3)
                PreviewLabText("Modal body text rendered inside PreviewLabModal.", style = PreviewLabTheme.typography.body2)
                PreviewLabButton(text = "Action", onClick = {}, style = PreviewLabButtonStyle.Primary)
            }
        }
    }
}

/** PreviewLabTooltipBox; the case hovers the "Anchor" button so the tooltip is visible in the capture. */
@Composable
private fun TooltipCatalog() {
    Section("PreviewLabTooltipBox (tooltip shown on hover)") {
        Box(Modifier.fillMaxWidth().padding(top = 72.dp), contentAlignment = Alignment.Center) {
            PreviewLabTooltipBox(tooltip = "Tooltip text") {
                PreviewLabButton(text = "Anchor", onClick = {}, style = PreviewLabButtonStyle.Outlined)
            }
        }
    }
}

/** PreviewLabTabs + PreviewLabTabList + PreviewLabTab (first tab selected), shaped like PreviewLabTabPager. */
@Composable
private fun TabRowCatalog() {
    val titles = listOf("Fields", "Events", "KDoc", "Debug")
    Section("PreviewLabTabs / PreviewLabTabList (scrolling strip, as in PreviewLabTabPager)") {
        PreviewLabTabs(
            selectedTab = 0,
            onSelectedTabChange = {},
            orderedTabs = titles.indices.toList(),
        ) {
            Column {
                PreviewLabTabList(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    dividerColor = Color.Transparent,
                ) {
                    titles.forEachIndexed { index, title ->
                        PreviewLabTab(key = index) { PreviewLabText(title) }
                    }
                }
                PreviewLabHorizontalSeparator()
            }
        }
    }
    Section("PreviewLabTabs (default divider, disabled tab, tab panel)") {
        PreviewLabTabs(
            selectedTab = 1,
            onSelectedTabChange = {},
            orderedTabs = titles.indices.toList(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PreviewLabTabList {
                    titles.forEachIndexed { index, title ->
                        PreviewLabTab(key = index, enabled = index != 3) { PreviewLabText(title) }
                    }
                }
                titles.indices.forEach { index ->
                    PreviewLabTabPanel(key = index) {
                        PreviewLabText("Panel of ${titles[index]}", style = PreviewLabTheme.typography.body2)
                    }
                }
            }
        }
    }
}

/** PreviewLabCard / PreviewLabOutlinedCard (static and clickable). */
@Composable
private fun CardCatalog() {
    Section("Cards") {
        WrapRow {
            PreviewLabCard { CardBody("PreviewLabCard") }
            PreviewLabOutlinedCard { CardBody("PreviewLabOutlinedCard") }
        }
    }
    Section("Clickable cards") {
        WrapRow {
            PreviewLabCard(onClick = {}) { CardBody("Card(onClick)") }
            PreviewLabCard(onClick = {}, enabled = false) { CardBody("Card(disabled)") }
            PreviewLabOutlinedCard(onClick = {}) { CardBody("OutlinedCard(onClick)") }
            PreviewLabOutlinedCard(onClick = {}, enabled = false) { CardBody("OutlinedCard(disabled)") }
        }
    }
}

@Composable
private fun ColumnScope.CardBody(title: String) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PreviewLabText(title, style = PreviewLabTheme.typography.label1)
        PreviewLabText("Card body", style = PreviewLabTheme.typography.body3)
    }
}

/** PreviewLabHorizontalSeparator / PreviewLabVerticalSeparator / PreviewLabDivider (scope extensions). */
@Composable
private fun DividerCatalog() {
    Section("PreviewLabHorizontalSeparator") {
        Column {
            PreviewLabText("Above")
            PreviewLabHorizontalSeparator()
            PreviewLabText("Below")
            PreviewLabDivider()
            PreviewLabText("Below ColumnScope.PreviewLabDivider()")
        }
    }
    Section("PreviewLabVerticalSeparator") {
        Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            PreviewLabText("Left")
            Spacer(Modifier.width(8.dp))
            PreviewLabVerticalSeparator()
            Spacer(Modifier.width(8.dp))
            PreviewLabText("Right")
            Spacer(Modifier.width(8.dp))
            PreviewLabDivider()
            Spacer(Modifier.width(8.dp))
            PreviewLabText("RowScope.PreviewLabDivider()")
        }
    }
    Section("Colored / thick separator") {
        PreviewLabHorizontalSeparator(thickness = 4.dp, color = PreviewLabTheme.colors.primary)
    }
}

/** ToastHost + rememberToastHostState: one host per [ToastType] and one stacked host. */
@Composable
private fun ToastCatalog() {
    Section("ToastHost: one toast per type") {
        ToastType.entries.forEach { type ->
            val toastHostState = rememberToastHostState()
            LaunchedEffect(Unit) {
                toastHostState.show(
                    message = "$type toast",
                    type = type,
                    action = if (type == ToastType.Default) ToastAction(label = "Show Detail", onClick = {}) else null,
                    duration = ToastDuration.Indefinite,
                )
            }
            Box(Modifier.fillMaxWidth().height(72.dp)) {
                ToastHost(state = toastHostState)
            }
        }
    }
    Section("ToastHost: stacked toasts (3 visible)") {
        val stackedState = rememberToastHostState()
        LaunchedEffect(Unit) {
            stackedState.show("First event", duration = ToastDuration.Indefinite)
            stackedState.show("Second event", duration = ToastDuration.Indefinite)
            stackedState.show("Third event (newest)", duration = ToastDuration.Indefinite, showCloseButton = false)
        }
        Box(Modifier.fillMaxWidth().height(120.dp)) {
            ToastHost(state = stackedState, maxVisibleToasts = 3)
        }
    }
}

/** PreviewLabColorPicker (HSV square + hue + alpha sliders). */
@Composable
private fun ColorPickerCatalog() {
    Section("PreviewLabColorPicker") {
        WrapRow {
            PreviewLabColorPicker(
                color = Color(0xFF3F51B5),
                onColorSelected = {},
                modifier = Modifier.width(240.dp).aspectRatio(3f / 2f),
            )
            PreviewLabColorPicker(
                color = Color(0x80FF9800),
                onColorSelected = {},
                modifier = Modifier.width(240.dp).aspectRatio(3f / 2f),
            )
        }
    }
}

/** PreviewLabListHeader + PreviewLabListItem (selected / normal / disabled / leading content). */
@Composable
private fun ListCatalog() {
    Section("PreviewLabListHeader / PreviewLabListItem") {
        Column(Modifier.width(300.dp)) {
            PreviewLabListHeader(
                title = "3 items",
                actions = {
                    PreviewLabIconButton(
                        painter = painterResource(PreviewLabUiRes.drawable.icon_delete),
                        contentDescription = "Clear",
                        onClick = {},
                    )
                },
            )
            PreviewLabListItem(title = "Selected item", isSelected = true, onSelect = {})
            PreviewLabListItem(title = "Normal item", isSelected = false, onSelect = {})
            PreviewLabListItem(title = "Disabled item", isSelected = false, onSelect = {}, isEnabled = false)
            PreviewLabListItem(
                title = "Item with leading content",
                isSelected = false,
                onSelect = {},
                leadingContent = {
                    PreviewLabIcon(
                        painter = painterResource(PreviewLabUiRes.drawable.icon_check),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}

/** PreviewLabText in every typography style of the theme. */
@Composable
private fun TextCatalog() {
    val typography = PreviewLabTheme.typography
    val styles = listOf(
        "h1" to typography.h1,
        "h2" to typography.h2,
        "h3" to typography.h3,
        "h4" to typography.h4,
        "body1" to typography.body1,
        "body2" to typography.body2,
        "body3" to typography.body3,
        "label1" to typography.label1,
        "label2" to typography.label2,
        "label3" to typography.label3,
        "button" to typography.button,
        "input" to typography.input,
    )
    Section("PreviewLabText typography") {
        styles.forEach { (name, style) ->
            PreviewLabText("$name — The quick brown fox jumps over the lazy dog", style = style)
        }
    }
    Section("PreviewLabText colors") {
        PreviewLabText("text (default)", color = PreviewLabTheme.colors.text)
        PreviewLabText("textSecondary", color = PreviewLabTheme.colors.textSecondary)
        PreviewLabText("textDisabled", color = PreviewLabTheme.colors.textDisabled)
        PreviewLabText("muted", color = PreviewLabTheme.colors.muted)
        PreviewLabText("primary", color = PreviewLabTheme.colors.primary)
        PreviewLabText("error", color = PreviewLabTheme.colors.error)
    }
}
