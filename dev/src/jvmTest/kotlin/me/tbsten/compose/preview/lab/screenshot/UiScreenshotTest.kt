@file:OptIn(
    ExperimentalTestApi::class,
    ExperimentalComposePreviewLabApi::class,
    ExperimentalTime::class,
)

package me.tbsten.compose.preview.lab.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kotest.core.spec.style.StringSpec
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.PreviewsForUiDebug
import me.tbsten.compose.preview.lab.featuredFilesForDebug
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.ByteField
import me.tbsten.compose.preview.lab.field.ColorField
import me.tbsten.compose.preview.lab.field.ComposableField
import me.tbsten.compose.preview.lab.field.ComposableFieldValue
import me.tbsten.compose.preview.lab.field.DoubleField
import me.tbsten.compose.preview.lab.field.DpField
import me.tbsten.compose.preview.lab.field.DpOffsetField
import me.tbsten.compose.preview.lab.field.DpSizeField
import me.tbsten.compose.preview.lab.field.EnumField
import me.tbsten.compose.preview.lab.field.FixedField
import me.tbsten.compose.preview.lab.field.FloatField
import me.tbsten.compose.preview.lab.field.InstantField
import me.tbsten.compose.preview.lab.field.IntField
import me.tbsten.compose.preview.lab.field.ListField
import me.tbsten.compose.preview.lab.field.LongField
import me.tbsten.compose.preview.lab.field.ModifierField
import me.tbsten.compose.preview.lab.field.OffsetField
import me.tbsten.compose.preview.lab.field.PolymorphicField
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.field.ScreenSizeField
import me.tbsten.compose.preview.lab.field.SelectableField
import me.tbsten.compose.preview.lab.field.SetField
import me.tbsten.compose.preview.lab.field.SizeField
import me.tbsten.compose.preview.lab.field.SpField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.combined
import me.tbsten.compose.preview.lab.field.mark
import me.tbsten.compose.preview.lab.field.modifier.ModifierFieldValue
import me.tbsten.compose.preview.lab.field.modifier.padding
import me.tbsten.compose.preview.lab.field.nullable
import me.tbsten.compose.preview.lab.field.splitedOf
import me.tbsten.compose.preview.lab.field.transform
import me.tbsten.compose.preview.lab.field.withHint
import me.tbsten.compose.preview.lab.field.withIncrementDecrementHint
import me.tbsten.compose.preview.lab.field.withPredefinedColorHint
import me.tbsten.compose.preview.lab.gallery.AllGroupName
import me.tbsten.compose.preview.lab.gallery.PreviewLabGallery
import me.tbsten.compose.preview.lab.gallery.PreviewLabGalleryState
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.previewlab.PreviewLabState
import me.tbsten.compose.preview.lab.previewsForUiDebug

/**
 * Screenshot harness for the library UI (PreviewLab screen, Gallery screen and every Field's `View()`).
 *
 * The screenshots are used as BEFORE/AFTER comparisons for the UI-kit migration, so this spec only
 * references the public API of `core`, `field`, `preview-lab`, `gallery` and the dev module
 * (never `me.tbsten.compose.preview.lab.ui.*`).
 *
 * Run:
 * ```
 * ./gradlew :dev:jvmTest --tests 'me.tbsten.compose.preview.lab.screenshot.*' \
 *     -Dkotest.tags='Screenshot' -Dpreviewlab.screenshot.dir=/absolute/output/dir
 * ```
 *
 * Every case is skipped unless `previewlab.screenshot.dir` is set (see [ScreenshotOutput]).
 *
 * ## How the full PreviewLab UI is rendered headlessly
 * `PreviewLab { }` renders its header / inspector pane / toast host only when
 * `LocalIsInPreviewLabGalleryCardBody == false` and `enable == true`. [ScreenshotHost] keeps that
 * local `false`, provides a `ViewModelStoreOwner` / `LifecycleOwner` and forces a test-controlled
 * [PreviewLabState] through `LocalEnforcePreviewLabState`, so tests can switch inspector tabs
 * (`state.selectedTabIndex`) or collapse the pane (`state.isInspectorPanelVisible`).
 * The desktop test clock auto-advances during `awaitIdle()`, which settles tab-pager / toast
 * animations; pending `delay()`s (toast auto-dismiss, "N seconds ago" ticker) stay pending.
 *
 * ## How standalone fields get the theme
 * Field views need the ui-module theme that `PreviewLab` applies internally. [FieldViewHost]
 * therefore renders a chrome-less `PreviewLab` (no header, no inspector tabs, no grid) and puts the
 * field's `View()` into its content area; the screenshot is cropped to that node by test tag.
 */
class UiScreenshotTest :
    StringSpec({

        // ------------------------------------------------------------------ (previewlab) full screen

        previewLabCase(
            title = "PreviewLab screen: Fields preview, Fields tab",
            fileName = "01-previewlab-Fields-fields-tab.png",
            preview = PreviewsForUiDebug.Fields,
        )
        previewLabCase(
            title = "PreviewLab screen: Fields preview, Events tab (no events)",
            fileName = "02-previewlab-Fields-events-tab-empty.png",
            preview = PreviewsForUiDebug.Fields,
        ) { state -> state.selectedTabIndex = 1 }
        previewLabCase(
            title = "PreviewLab screen: Fields preview, KDoc tab",
            fileName = "03-previewlab-Fields-kdoc-tab.png",
            preview = PreviewsForUiDebug.Fields,
        ) { state -> state.selectedTabIndex = 2 }
        previewLabCase(
            title = "PreviewLab screen: Fields preview, header with inspector pane collapsed",
            fileName = "04-previewlab-Fields-inspector-collapsed.png",
            preview = PreviewsForUiDebug.Fields,
        ) { state -> state.isInspectorPanelVisible = false }
        previewLabCase(
            title = "PreviewLab screen: Events preview after clicking 3 items (toasts visible)",
            fileName = "05-previewlab-Events-toasts.png",
            preview = PreviewsForUiDebug.Events,
        ) { clickSampleItems(3) }
        previewLabCase(
            title = "PreviewLab screen: Events preview, Events tab with 3 events",
            fileName = "06-previewlab-Events-events-tab.png",
            preview = PreviewsForUiDebug.Events,
        ) { state ->
            clickSampleItems(3)
            state.selectedTabIndex = 1
            awaitIdle()
            // let the "new event" highlight fade (800ms delay + 300ms tween) so the list is stable
            mainClock.advanceTimeBy(1_500)
        }
        previewLabCase(
            title = "PreviewLab screen: Layouts preview",
            fileName = "07-previewlab-Layouts.png",
            preview = PreviewsForUiDebug.Layouts,
        )
        previewLabCase(
            title = "PreviewLab screen: ScreenSize preview (all presets)",
            fileName = "08-previewlab-ScreenSize.png",
            preview = PreviewsForUiDebug.ScreenSize,
        )
        previewLabCase(
            title = "PreviewLab screen: LoginForm preview",
            fileName = "09-previewlab-LoginForm.png",
            preview = PreviewsForUiDebug.LoginForm,
        )
        previewLabCase(
            title = "PreviewLab screen: ProfileSettings preview",
            fileName = "10-previewlab-ProfileSettings.png",
            preview = PreviewsForUiDebug.ProfileSettings,
        )
        previewLabCase(
            title = "PreviewLab screen: ModifierAndComposableField preview",
            fileName = "11-previewlab-ModifierAndComposableField.png",
            preview = PreviewsForUiDebug.ModifierAndComposableField,
        )
        previewLabCase(
            title = "PreviewLab screen: CustomInspectorTab preview, custom Debug tab selected",
            fileName = "12-previewlab-CustomInspectorTab-debug-tab.png",
            preview = PreviewsForUiDebug.CustomInspectorTab,
        ) { state -> state.selectedTabIndex = 3 }
        previewLabCase(
            title = "PreviewLab screen: small breakpoint (480px) with inspector buttons",
            fileName = "13-previewlab-Fields-small-breakpoint.png",
            preview = PreviewsForUiDebug.Fields,
            width = 480,
            height = 900,
        )
        previewLabCase(
            title = "PreviewLab screen: small breakpoint (480px), Fields inspector modal opened",
            fileName = "14-previewlab-Fields-small-breakpoint-modal.png",
            preview = PreviewsForUiDebug.Fields,
            width = 480,
            height = 900,
        ) { state -> state.selectedTabIndex = 0 }

        // ------------------------------------------------------------------ (gallery)

        galleryCase(
            title = "Gallery: initial grid, nothing selected",
            fileName = "15-gallery-initial.png",
        )
        galleryCase(
            title = "Gallery: Fields preview selected",
            fileName = "16-gallery-selected-Fields.png",
            state = { PreviewLabGalleryState(AllGroupName to PreviewsForUiDebug.Fields) },
        )
        galleryCase(
            title = "Gallery: search bar with query \"Button\"",
            fileName = "17-gallery-search-Button.png",
        ) { state -> state.onQueryChange("Button") }
        galleryCase(
            title = "Gallery: compare panel (Events + ButtonPrimary)",
            fileName = "18-gallery-compare-panel.png",
            state = {
                PreviewLabGalleryState(AllGroupName to PreviewsForUiDebug.Events).apply {
                    addToComparePanel(AllGroupName, PreviewsForUiDebug.ButtonPrimary)
                }
            },
        )
        galleryCase(
            title = "Gallery: small breakpoint (480px), no sidebar",
            fileName = "19-gallery-small-breakpoint.png",
            width = 480,
            height = 900,
        )

        // ------------------------------------------------------------------ (fields) every field View()

        fieldViewCase("StringField", "20-field-StringField.png") {
            StringField("stringValue", "Hello Compose Preview Lab")
        }
        fieldViewCase("StringField (multiline value)", "21-field-StringField-multiline.png") {
            StringField("multilineValue", "First line\nSecond line\nThird line")
        }
        fieldViewCase("IntField", "22-field-IntField.png") { IntField("intValue", 42) }
        fieldViewCase("LongField", "23-field-LongField.png") { LongField("longValue", 1_234_567_890_123L) }
        fieldViewCase("FloatField", "24-field-FloatField.png") { FloatField("floatValue", 1.5f) }
        fieldViewCase("DoubleField", "25-field-DoubleField.png") { DoubleField("doubleValue", 3.14159) }
        fieldViewCase("ByteField", "26-field-ByteField.png") { ByteField("byteValue", 7) }
        fieldViewCase("BooleanField", "27-field-BooleanField.png") { BooleanField("booleanValue", true) }
        fieldViewCase("SelectableField (DROPDOWN)", "28-field-SelectableField-dropdown.png") {
            colorSelectableField()
        }
        fieldViewCase(
            title = "SelectableField (DROPDOWN) with the dropdown menu opened",
            fileName = "29-field-SelectableField-dropdown-opened.png",
            captureWholeWindow = true,
            interact = { onNodeWithText("Red").performClick() },
        ) { colorSelectableField() }
        fieldViewCase("SelectableField (CHIPS)", "30-field-SelectableField-chips.png") {
            SelectableField<String>("size", type = SelectableField.Type.CHIPS) {
                choice("Small", isDefault = false)
                choice("Medium", isDefault = true)
                choice("Large")
                choice("Extra Large")
            }
        }
        fieldViewCase("SelectableField (RADIO)", "31-field-SelectableField-radio.png") {
            SelectableField<String>("alignment", type = SelectableField.Type.RADIO) {
                choice("Start", isDefault = true)
                choice("Center")
                choice("End")
            }
        }
        fieldViewCase(
            title = "SelectableField with the field view menu (⋮) opened",
            fileName = "32-field-SelectableField-view-menu-opened.png",
            captureWholeWindow = true,
            interact = { clickSmallestClickable() },
        ) { colorSelectableField() }
        fieldViewCase("EnumField (DROPDOWN)", "33-field-EnumField.png") {
            EnumField<ScreenshotEnum>("enumValue", ScreenshotEnum.Beta)
        }
        fieldViewCase("ColorField (inline HSV picker)", "34-field-ColorField.png") {
            ColorField("colorValue", Color(0xFF3F51B5))
        }
        fieldCase(
            title = "ColorField with the field view menu (⋮, Reset Value) opened",
            fileName = "35-field-ColorField-menu-opened.png",
            captureWholeWindow = true,
            interact = { clickSmallestClickable() },
        ) {
            val field = remember { ColorField("colorValue", Color(0xFF3F51B5)) }
            field.View(menuItems = listOf(PreviewLabField.ViewMenuItem.ResetValue(field)))
        }
        fieldViewCase("ColorField.withPredefinedColorHint()", "36-field-ColorField-withPredefinedColorHint.png") {
            ColorField("tint", Color.Magenta).withPredefinedColorHint()
        }
        fieldViewCase("DpField", "37-field-DpField.png") { DpField("dpValue", 16.dp) }
        fieldViewCase("SpField", "38-field-SpField.png") { SpField("spValue", 20.sp) }
        fieldViewCase("DpOffsetField", "39-field-DpOffsetField.png") { DpOffsetField("dpOffsetValue", DpOffset(8.dp, 16.dp)) }
        fieldViewCase("DpSizeField", "40-field-DpSizeField.png") { DpSizeField("dpSizeValue", DpSize(200.dp, 120.dp)) }
        fieldViewCase("OffsetField", "41-field-OffsetField.png") { OffsetField("offsetValue", Offset(20f, 10f)) }
        fieldViewCase("SizeField", "42-field-SizeField.png") { SizeField("sizeValue", Size(80f, 60f)) }
        fieldViewCase("ScreenSizeField", "43-field-ScreenSizeField.png") {
            ScreenSizeField(sizes = ScreenSize.SmartphoneAndDesktops)
        }
        fieldViewCase("ListField (summary card)", "44-field-ListField.png") { fruitsListField() }
        fieldViewCase(
            title = "ListField with the editor modal opened",
            fileName = "45-field-ListField-editor-opened.png",
            captureWholeWindow = true,
            windowHeight = 900,
            interact = { clickClickableWithText("Apple") },
        ) { fruitsListField() }
        fieldViewCase("SetField (summary card)", "46-field-SetField.png") {
            SetField(
                label = "fruits",
                initialValue = setOf("Apple", "Banana", "Cherry"),
                elementField = { StringField(label, initialValue) },
            )
        }
        fieldCase("NullableField (null and non-null)", "47-field-NullableField.png") {
            val nullField = remember { StringField("nullableString", "value").nullable(initialValue = null) }
            val nonNullField = remember { IntField("nullableInt", 42).nullable() }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                nullField.View()
                nonNullField.View()
            }
        }
        fieldViewCase("CombinedField (3 fields)", "48-field-CombinedField.png") {
            combined(
                label = "uiState",
                field1 = StringField("str", "text"),
                field2 = IntField("int", 3),
                field3 = BooleanField("bool", true),
                combine = { str, int, bool -> Triple(str, int, bool) },
                split = { splitedOf(it.first, it.second, it.third) },
            )
        }
        fieldViewCase("ComposableField", "49-field-ComposableField.png") {
            ComposableField("children", initialValue = ComposableFieldValue.SimpleText)
        }
        fieldViewCase("ModifierField (mark + padding)", "50-field-ModifierField.png", windowHeight = 900) {
            ModifierField("modifier", initialValue = ModifierFieldValue.mark().padding(all = 8.dp))
        }
        fieldViewCase("PolymorphicField (Loading / Stable / Error)", "51-field-PolymorphicField.png") {
            PolymorphicField<ScreenshotState>(
                label = "state",
                initialValue = ScreenshotState.Stable("Success !"),
                fields = listOf(
                    FixedField("Loading", ScreenshotState.Loading),
                    combined(
                        label = "Stable",
                        field1 = StringField("data", "Success !"),
                        combine = { data -> ScreenshotState.Stable(data) },
                        split = { splitedOf(it.data) },
                    ),
                    combined(
                        label = "Error",
                        field1 = StringField("message", "Something went wrong"),
                        combine = { message -> ScreenshotState.Error(message) },
                        split = { splitedOf(it.message) },
                    ),
                ),
            )
        }
        fieldViewCase("FixedField", "52-field-FixedField.png") { FixedField("fixedValue", "constant") }
        fieldViewCase("TransformField (String -> Int?)", "53-field-TransformField.png") {
            StringField("number", "42").transform(
                transform = { it.toIntOrNull() },
                reverse = { it?.toString() ?: "" },
            )
        }
        fieldViewCase("WithHintField (StringField.withHint)", "54-field-WithHintField.png") {
            StringField("Text.text", "Hello World").withHint(
                "Empty" to "",
                "Simple" to "Hello World",
                "Long" to "Very " + "long ".repeat(20) + "text",
            )
        }
        fieldViewCase("WithHintField (IntField.withIncrementDecrementHint)", "55-field-WithHintField-increment.png") {
            IntField("count", 1).withIncrementDecrementHint()
        }
        fieldViewCase("InstantField", "56-field-InstantField.png") {
            InstantField("instantValue", Instant.fromEpochMilliseconds(1_700_000_000_000L))
        }
    })

// ---------------------------------------------------------------------- helpers: previewlab

private fun StringSpec.previewLabCase(
    title: String,
    fileName: String,
    preview: PreviewsForUiDebug,
    width: Int = 1280,
    height: Int = 800,
    settle: suspend ComposeUiTest.(PreviewLabState) -> Unit = {},
) = screenshotCase(title = title, group = "previewlab", fileName = fileName) {
    val state = PreviewLabState()
    runDesktopComposeUiTest(width = width, height = height) {
        setContent {
            ScreenshotHost(state = state, preview = preview) {
                preview.content()
            }
        }
        awaitIdle()
        settle(this, state)
        awaitIdle()
        ScreenshotOutput.save(captureWindow(), fileName, title, "previewlab")
    }
}

/** Clicks `item:0 .. item:(count-1)` of the dev `SampleScreen` list (each click emits an event). */
private suspend fun ComposeUiTest.clickSampleItems(count: Int) {
    repeat(count) { index ->
        onNodeWithTag("item:$index").performClick()
        awaitIdle()
    }
}

// ---------------------------------------------------------------------- helpers: gallery

private fun StringSpec.galleryCase(
    title: String,
    fileName: String,
    width: Int = 1280,
    height: Int = 800,
    state: () -> PreviewLabGalleryState = { PreviewLabGalleryState() },
    settle: suspend ComposeUiTest.(PreviewLabGalleryState) -> Unit = {},
) = screenshotCase(title = title, group = "gallery", fileName = fileName) {
    val galleryState = state()
    runDesktopComposeUiTest(width = width, height = height) {
        setContent {
            ScreenshotHost {
                PreviewLabGallery(
                    previewList = previewsForUiDebug,
                    featuredFileList = featuredFilesForDebug,
                    state = galleryState,
                )
            }
        }
        awaitIdle()
        settle(this, galleryState)
        awaitIdle()
        ScreenshotOutput.save(captureWindow(), fileName, title, "gallery")
    }
}

// ---------------------------------------------------------------------- helpers: fields

private const val FieldViewTag = "screenshot-field-view"
private val FieldViewWidth: Dp = 420.dp
private const val FieldWindowWidth = 520

/**
 * Chrome-less `PreviewLab` used only to obtain the library theme for a standalone field view:
 * no header, no inspector tabs, inspector pane hidden, grid disabled. The content area hosts the
 * field inside `Box(Modifier.width(420.dp).padding(16.dp))` tagged with [FieldViewTag].
 */
@Composable
private fun FieldViewHost(content: @Composable () -> Unit) {
    val state = remember {
        PreviewLabState(
            initialIsInspectorPanelVisible = false,
            initialGridSize = null,
        )
    }
    ScreenshotHost(state = state) {
        PreviewLab(
            state = state,
            screenSizes = listOf(ScreenSize(FieldViewWidth, 3000.dp)),
            showScreenSizeField = false,
            isHeaderShow = false,
            inspectorTabs = emptyList(),
            enable = true,
            isInPreviewLabGalleryCardBody = false,
        ) {
            Box(
                modifier = Modifier
                    .testTag(FieldViewTag)
                    .width(FieldViewWidth)
                    .padding(16.dp),
            ) {
                content()
            }
        }
    }
}

private fun StringSpec.fieldCase(
    title: String,
    fileName: String,
    windowHeight: Int = 700,
    captureWholeWindow: Boolean = false,
    interact: suspend ComposeUiTest.() -> Unit = {},
    content: @Composable () -> Unit,
) = screenshotCase(title = title, group = "fields", fileName = fileName) {
    runDesktopComposeUiTest(width = FieldWindowWidth, height = windowHeight) {
        setContent { FieldViewHost { content() } }
        awaitIdle()
        interact(this)
        awaitIdle()
        val image = if (captureWholeWindow) {
            captureWindow()
        } else {
            onNodeWithTag(FieldViewTag, useUnmergedTree = true).captureToImage()
        }
        ScreenshotOutput.save(
            image = image,
            fileName = fileName,
            title = title,
            group = "fields",
            note = if (captureWholeWindow) "whole ${FieldWindowWidth}x$windowHeight window (popup/dialog open)" else null,
        )
    }
}

/** Renders `field.View()` for the field created by [build] (remembered across recompositions). */
private fun StringSpec.fieldViewCase(
    title: String,
    fileName: String,
    windowHeight: Int = 700,
    captureWholeWindow: Boolean = false,
    interact: suspend ComposeUiTest.() -> Unit = {},
    build: () -> PreviewLabField<*>,
) = fieldCase(
    title = title,
    fileName = fileName,
    windowHeight = windowHeight,
    captureWholeWindow = captureWholeWindow,
    interact = interact,
) {
    val field = remember { build() }
    field.View()
}

private fun colorSelectableField() = SelectableField<Color>("backgroundColor") {
    choice(Color.Red, "Red", isDefault = true)
    choice(Color.Blue, "Blue")
    choice(Color.Green, "Green")
}

private fun fruitsListField() = ListField(
    label = "fruits",
    initialValue = listOf("Apple", "Banana", "Cherry"),
    elementField = { StringField(label, initialValue) },
)

private enum class ScreenshotEnum { Alpha, Beta, Gamma }

private sealed interface ScreenshotState {
    data object Loading : ScreenshotState
    data class Stable(val data: String) : ScreenshotState
    data class Error(val message: String) : ScreenshotState
}
