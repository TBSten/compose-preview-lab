package me.tbsten.compose.preview.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.event.withEvent
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.ColorField
import me.tbsten.compose.preview.lab.field.ComposableField
import me.tbsten.compose.preview.lab.field.ComposableFieldValue
import me.tbsten.compose.preview.lab.field.DpOffsetField
import me.tbsten.compose.preview.lab.field.DpSizeField
import me.tbsten.compose.preview.lab.field.EnumField
import me.tbsten.compose.preview.lab.field.FloatField
import me.tbsten.compose.preview.lab.field.IntField
import me.tbsten.compose.preview.lab.field.ModifierField
import me.tbsten.compose.preview.lab.field.SelectableField
import me.tbsten.compose.preview.lab.field.SpField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.combined
import me.tbsten.compose.preview.lab.field.mark
import me.tbsten.compose.preview.lab.field.modifier.ModifierFieldValue
import me.tbsten.compose.preview.lab.field.nullable
import me.tbsten.compose.preview.lab.field.provideDefaultCompositionLocalFields
import me.tbsten.compose.preview.lab.field.splitedOf
import me.tbsten.compose.preview.lab.field.withHint

enum class PreviewsForUiDebug(
    override val id: String,
    override val filePath: String? = "src/commonMain/kotlin/me/tbsten/example/$id.kt",
    override val displayName: String = id,
    override val content: @Composable (() -> Unit),
) : CollectedPreview {
    Fields(
        "Fields",
        content = {
            PreviewLab {
                CompositionLocalProvider(
                    *provideDefaultCompositionLocalFields(),
                ) {
                    SampleScreen(
                        title = "Fields",
                        onListItemClick = {},
                    ) {
                        LazyColumn(
                            contentPadding = it.plus(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            header("Primitive types")
                            item {
                                val stringValue =
                                    fieldValue { StringField("stringValue", "input some text") }
                                Text("intValue: $stringValue")
                            }
                            item {
                                val intValue = fieldValue { IntField("intValue", 0) }
                                Text("intValue: $intValue")
                            }
                            item {
                                val floatValue = fieldValue { FloatField("floatField", 0f) }
                                Text("floatField: $floatValue")
                            }
                            item {
                                val floatValue = fieldValue { FloatField("floatField", 0f) }
                                Text("floatField: $floatValue")
                            }
                            item {
                                val booleanValue = fieldValue { BooleanField("booleanValue", false) }
                                Text("booleanValue: $booleanValue")
                            }

                            header("ComposeValueField")
                            item {
                                val dpOffsetValue = fieldValue {
                                    DpOffsetField(
                                        "dpOffsetValue",
                                        DpOffset(0.dp, 0.dp),
                                    )
                                }
                                val dpSizeValue = fieldValue {
                                    DpSizeField(
                                        "dpSizeValue",
                                        DpSize(200.dp, 120.dp),
                                    )
                                }
                                val spValue = fieldValue {
                                    SpField("spValue", 20.sp)
                                }
                                val colorValue = fieldValue {
                                    ColorField("colorValue", Color.Yellow)
                                }
                                val textModifierValue = fieldValue {
                                    ModifierField("modifier")
                                }
                                val childrenValue = fieldValue {
                                    val choices = listOf(
                                        ComposableFieldValue("Blue") {
                                            Box(Modifier.background(Color.Blue).size(100.dp))
                                        },
                                    ) +
                                        ComposableFieldValue.DefaultChoices
                                    ComposableField(
                                        "children",
                                        initialValue = choices.first(),
                                        choices = choices,
                                    )
                                }

                                Column(
                                    Modifier
                                        .offset(x = dpOffsetValue.x, y = dpOffsetValue.y)
                                        .background(colorValue)
                                        .size(dpSizeValue),
                                ) {
                                    Text("spValue", fontSize = spValue, modifier = textModifierValue)
//                                Divider()
                                    HorizontalDivider()
                                    childrenValue()
                                }
                            }

                            header("NullableField")
                            item {
                                val nullableStringField =
                                    fieldValue {
                                        StringField("nullableStringField", "").nullable(
                                            initialValue = null,
                                        )
                                    }
                                Text("nullableStringField: ${nullableStringField ?: "🚨🚨🚨 is null 🚨🚨🚨"}")
                            }

                            header("SelectableField")
                            item {
                                val backgroundColor = fieldValue {
                                    SelectableField("backgroundColor") {
                                        choice(Color.Red, isDefault = true)
                                        choice(Color.Blue)
                                        choice(Color.Green)
                                    }
                                }
                                Box(
                                    Modifier
                                        .background(
                                            color = backgroundColor,
                                        )
                                        .size(50.dp),
                                )
                            }
                            item {
                                Text(
                                    "myEnumValue: ${
                                        fieldValue { EnumField<MyEnum>("myEnumValue", MyEnum.A) }
                                    }",
                                )
                                Text(
                                    "myEnumValue (chip UI): ${
                                        fieldValue {
                                            EnumField<MyEnum>(
                                                "myEnumValue",
                                                MyEnum.A,
                                                type = SelectableField.Type.CHIPS,
                                            )
                                        }
                                    }",
                                )
                            }
                            header("With Hint")
                            item {
                                Text(
                                    text = "StringField value: " +
                                        "\"" +
                                        fieldValue {
                                            StringField("Text.text", "")
                                                .withHint(
                                                    "Empty" to "",
                                                    "Very long" to "very " + "long ".repeat(100) + "text",
                                                    "Simple" to "Hello World",
                                                )
                                        } +
                                        "\"",
                                )
                            }
                            item {
                                Text(
                                    text = "StringField value: " +
                                        "\"" +
                                        fieldValue {
                                            StringField("Text.text", "")
                                        } +
                                        "\"",
                                )
                            }

                            header("Combined Field")
                            item {
                                class UiState(val str: String, val int: Int, val bool: Boolean)

                                val uiStateField by fieldState {
                                    combined(
                                        "uiState",
                                        field1 = StringField("str", ""),
                                        field2 = IntField("int", 0),
                                        field3 = BooleanField("bool", false),
                                        combine = { str, int, bool -> UiState(str, int, bool) },
                                        split = { splitedOf(it.str, it.int, it.bool) },
                                    )
                                }

                                Text("uiState")
                                Text("  str: ${uiStateField.str}")
                                Text("  int: ${uiStateField.int}")
                                Text("  bool: ${uiStateField.bool}")
                            }
                        }
                    }
                }
            }
        },
    ),
    Events(
        "Events",
        content = {
            PreviewLab {
                SampleScreen(
                    title = "Events",
                    onListItemClick = { onEvent("Click item $it") },
                )
            }
        },
    ),
    Layouts(
        "Layouts",
        content = {
            PreviewLab {
                SampleScreen(
                    title = "Layouts",
                    onListItemClick = { },
                )
            }
        },
    ),
    ScreenSize(
        "ScreenSize",
        content = {
            PreviewLab(
                screenSizes = me.tbsten.compose.preview.lab.field.ScreenSize.AllPresets,
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                0f to Color.Green,
                                1f to Color.Blue,
                                start = Offset.Zero,
                                end = Offset.Infinite,
                            ),
                        )
                        .fillMaxSize(),
                )
            }
        },
    ),
    WithoutPreviewLab(
        "WithoutPreviewLab",
        content = {
            Text("Without PreviewLab { }")
        },
    ),
    ButtonPrimary(
        "ButtonPrimary",
        "com.example.ui.components.ButtonPrimary",
        content = {
            PreviewLab {
                SampleScreen(
                    title = "Primary Button",
                    onListItemClick = {},
                ) {
                    LazyColumn(
                        contentPadding = it.plus(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Button(
                                onClick = { onEvent("onClick") },
                                modifier = fieldValue { ModifierField("modifier", ModifierFieldValue) },
                                enabled = fieldValue { BooleanField("enabled", true) },
                            ) {
                                Text(
                                    text = fieldValue { StringField("Text.text", "Primary Button") },
                                    modifier = fieldValue { ModifierField("Text.modifier") },
                                )
                            }
                        }
                    }
                }
            }
        },
    ),
    ButtonSecondary(
        "ButtonSecondary",
        "com.example.ui.components.ButtonSecondary",
        content = {
            PreviewLab {
                SampleScreen(
                    title = "Secondary Button",
                    onListItemClick = {},
                ) {
                    LazyColumn(
                        contentPadding = it.plus(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            OutlinedButton(onClick = { onEvent("SecondaryButton.onClick") }) {
                                Text("Secondary Button")
                            }
                        }
                    }
                }
            }
        },
    ),
    HeadingText(
        "HeadingText",
        "com.example.ui.components.text.HeadingText",
        content = {
            PreviewLab {
                SampleScreen(
                    title = "Text Heading",
                    onListItemClick = {},
                ) {
                    LazyColumn(
                        contentPadding = it.plus(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Text(
                                "Heading Text",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    }
                }
            }
        },
    ),
    LoginForm(
        "LoginForm",
        "com.example.screens.login.LoginForm",
        content = {
            PreviewLab {
                SampleScreen(
                    title = "Login Form",
                    onListItemClick = {},
                ) {
                    LazyColumn(
                        contentPadding = it.plus(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Text("Login Form Preview")
                        }
                    }
                }
            }
        },
    ),
    ProfileSettings(
        "ProfileSettings",
        "com.example.screens.profile.ProfileSettings",
        content = {
            PreviewLab {
                SampleScreen(
                    title = "Profile Settings",
                    onListItemClick = {},
                ) {
                    LazyColumn(
                        contentPadding = it.plus(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Text("Profile Settings Preview")
                        }
                    }
                }
            }
        },
    ),
    ModifierAndComposableField(
        "ModifierAndComposableField",
        "Modifier/Composable Fields",
        content = {
            PreviewLab {
                Column {
                    Button(
                        onClick = withEvent("onClick"),
                        modifier = fieldValue {
                            ModifierField(
                                label = "Button.modifier",
                                initialValue = ModifierFieldValue
                                    .mark(color = Color.Blue),
                            )
                        },
                        content = {
                            fieldValue {
                                ComposableField(
                                    label = "Button.content",
                                    initialValue = ComposableFieldValue.SimpleText,
                                )
                            }
                                .invoke()
                        },
                    )

                    HorizontalDivider()

                    Scaffold(
                        topBar = fieldValue {
                            ComposableField(
                                label = "Scaffold.topBar",
                                initialValue = ComposableFieldValue.RedFillX80.copy(color = Color.Red),
                            )
                        },
                        bottomBar = fieldValue {
                            ComposableField(
                                label = "Scaffold.bottomBar",
                                initialValue = ComposableFieldValue.RedFillX80.copy(color = Color.Blue),
                            )
                        },
                        floatingActionButton = fieldValue {
                            ComposableField(
                                label = "Scaffold.floatingActionButton",
                                initialValue = ComposableFieldValue.Red64X64.copy(color = Color.Green),
                            )
                        },
                        content = { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                fieldValue {
                                    ComposableField(
                                        label = "Scaffold.content",
                                        initialValue = ComposableFieldValue.BodyText,
                                    )
                                }.invoke()
                            }
                        },
                    )
                }
            }
        },
    ),
    ;

    override val startLineNumber: Int? = null
    override val code: String? = null
}

val previewsForUiDebug = PreviewsForUiDebug.entries

private fun LazyListScope.header(title: String) {
    stickyHeader {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        HorizontalDivider()
    }
}

private enum class MyEnum {
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H,
    I,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SampleScreen(
    title: String,
    onListItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit = {
        DefaultSampleScreenContent(
            it,
            onListItemClick,
        )
    },
) = Scaffold(
    topBar = {
        TopAppBar(
            title = { Text(title) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.8f)),
//            modifier = Modifier.layoutLab("TopAppBar"),
        )
    },
    modifier = modifier,
    content = content,
)

@Composable
private fun DefaultSampleScreenContent(paddingValues: PaddingValues, onListItemClick: (Int) -> Unit) {
    LazyColumn(
        contentPadding = paddingValues.plus(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
//            .layoutLab("LazyColumn")
            .background(
                Brush.linearGradient(
                    0f to Color.Green,
                    1f to Color.Blue,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .fillMaxSize(),
    ) {
        items(20) { count ->
            Button(
                onClick = { onListItemClick(count) },
                modifier = Modifier
//                    .layoutLab("Item: $count")
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "Item $count",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

private fun PaddingValues.plus(top: Dp, bottom: Dp, start: Dp = 0.dp, end: Dp = 0.dp) = object : PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        this@plus.calculateLeftPadding(layoutDirection) + if (layoutDirection == LayoutDirection.Ltr) start else end

    override fun calculateTopPadding(): Dp = this@plus.calculateTopPadding() + top

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        this@plus.calculateRightPadding(layoutDirection) + if (layoutDirection == LayoutDirection.Ltr) end else start

    override fun calculateBottomPadding(): Dp = this@plus.calculateBottomPadding() + bottom
}
