package me.tbsten.compose.preview.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import me.tbsten.compose.preview.lab.field.BooleanField
import me.tbsten.compose.preview.lab.field.DpOffsetField
import me.tbsten.compose.preview.lab.field.DpSizeField
import me.tbsten.compose.preview.lab.field.EnumField
import me.tbsten.compose.preview.lab.field.FloatField
import me.tbsten.compose.preview.lab.field.IntField
import me.tbsten.compose.preview.lab.field.ScreenSize
import me.tbsten.compose.preview.lab.field.SelectableField
import me.tbsten.compose.preview.lab.field.SpField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.nullable

val previewsForUiDebug = listOf<CollectedPreview>(
    CollectedPreview("Fields", "src/commonMain/kotlin/me/tbsten/example/Fields.kt") {
        PreviewLab {
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
                                DpOffset(0.dp, 0.dp)
                            )
                        }
                        val dpSizeValue = fieldValue {
                            DpSizeField(
                                "dpSizeValue",
                                DpSize(20.dp, 20.dp)
                            )
                        }
                        val spValue = fieldValue {
                            SpField("spValue", 20.sp)
                        }
                        Box(
                            Modifier
                                .offset(x = dpOffsetValue.x, y = dpOffsetValue.y)
                                .background(Color.Yellow)
                                .size(dpSizeValue)
                        ) {
                            Text("spValue", fontSize = spValue)
                        }
                    }

                    header("NullableField")
                    item {
                        val nullableStringField =
                            fieldValue {
                                StringField("nullableStringField", "").nullable(
                                    initialValue = null
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
                                .size(50.dp)
                        )
                    }
                    item {
                        Text(
                            "myEnumValue: ${
                                fieldValue { EnumField<MyEnum>("myEnumValue", MyEnum.A) }
                            }"
                        )
                        Text(
                            "myEnumValue (chip UI): ${
                                fieldValue {
                                    EnumField<MyEnum>(
                                        "myEnumValue",
                                        MyEnum.A,
                                        type = SelectableField.Type.CHIPS
                                    )
                                }
                            }"
                        )
                    }
                }
            }
        }
    },
    CollectedPreview("Events", "src/commonMain/kotlin/me/tbsten/example/Events.kt") {
        PreviewLab {
            SampleScreen(
                title = "Events",
                onListItemClick = { onEvent("Click item $it") },
            )
        }
    },
    CollectedPreview("Layouts", "src/commonMain/kotlin/me/tbsten/example/Layouts.kt") {
        PreviewLab {
            SampleScreen(
                title = "Layouts",
                onListItemClick = { },
            )
        }
    },
    CollectedPreview("ScreenSize", "src/commonMain/kotlin/me/tbsten/example/ScreenSize.kt") {
        PreviewLab(
            screenSizes = ScreenSize.AllPresets,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            0f to Color.Green,
                            1f to Color.Blue,
                            start = Offset.Zero,
                            end = Offset.Infinite,
                        )
                    )
                    .fillMaxSize()
            )
        }
    },
    CollectedPreview(
        "Without PreviewLab",
        "src/commonMain/kotlin/me/tbsten/example/WithoutPreviewLab.kt",
    ) {
        Text("Without PreviewLab { }")
    },
    CollectedPreview(
        "com.example.ui.components.ButtonPrimary",
        "src/commonMain/kotlin/com/example/ui/components/ButtonPrimary.kt"
    ) {
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
                        Button(onClick = {}) {
                            Text("Primary Button")
                        }
                    }
                }
            }
        }
    },
    CollectedPreview(
        "com.example.ui.components.ButtonSecondary",
        "src/commonMain/kotlin/com/example/ui/components/ButtonSecondary.kt"
    ) {
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
    CollectedPreview(
        "com.example.ui.components.text.HeadingText",
        "src/commonMain/kotlin/com/example/ui/components/text/HeadingText.kt"
    ) {
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
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    },
    CollectedPreview(
        "com.example.screens.login.LoginForm",
        "src/commonMain/kotlin/com/example/screens/login/LoginForm.kt"
    ) {
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
    CollectedPreview(
        "com.example.screens.profile.ProfileSettings",
        "src/commonMain/kotlin/com/example/screens/profile/ProfileSettings.kt"
    ) {
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
)

private fun LazyListScope.header(title: String) {
    stickyHeader {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
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
            onListItemClick
        )
    },
) = Scaffold(
    topBar = {
        TopAppBar(
            title = { Text(title) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.8f)),
            modifier = Modifier.layoutLab("TopAppBar"),
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
            .layoutLab("LazyColumn")
            .background(
                Brush.linearGradient(
                    0f to Color.Green,
                    1f to Color.Blue,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                )
            )
            .fillMaxSize(),
    ) {
        items(20) { count ->
            Button(
                onClick = { onListItemClick(count) },
                modifier = Modifier
                    .layoutLab("Item: $count")
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

private fun PaddingValues.plus(top: Dp, bottom: Dp, start: Dp = 0.dp, end: Dp = 0.dp,) = object : PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        this@plus.calculateLeftPadding(layoutDirection) + if (layoutDirection == LayoutDirection.Ltr) start else end

    override fun calculateTopPadding(): Dp = this@plus.calculateTopPadding() + top

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        this@plus.calculateRightPadding(layoutDirection) + if (layoutDirection == LayoutDirection.Ltr) end else start

    override fun calculateBottomPadding(): Dp = this@plus.calculateBottomPadding() + bottom
}
