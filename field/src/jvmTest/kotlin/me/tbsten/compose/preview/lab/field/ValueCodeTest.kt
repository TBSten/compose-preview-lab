package me.tbsten.compose.preview.lab.field

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ValueCodeTest :
    StringSpec({

        // region BooleanField
        "BooleanField valueCode returns true for true value" {
            val field = BooleanField("test", true)
            field.valueCode() shouldBe "true"
        }

        "BooleanField valueCode returns false for false value" {
            val field = BooleanField("test", false)
            field.valueCode() shouldBe "false"
        }
        // endregion

        // region StringField
        "StringField valueCode returns quoted string" {
            val field = StringField("test", "Hello")
            field.valueCode() shouldBe "\"Hello\""
        }

        "StringField valueCode escapes double quotes" {
            val field = StringField("test", "Hello \"World\"")
            field.valueCode() shouldBe "\"Hello \\\"World\\\"\""
        }

        "StringField valueCode escapes backslashes" {
            val field = StringField("test", "path\\to\\file")
            field.valueCode() shouldBe "\"path\\\\to\\\\file\""
        }

        "StringField valueCode escapes newlines" {
            val field = StringField("test", "line1\nline2")
            field.valueCode() shouldBe "\"line1\\nline2\""
        }

        "StringField valueCode escapes carriage returns" {
            val field = StringField("test", "line1\rline2")
            field.valueCode() shouldBe "\"line1\\rline2\""
        }

        "StringField valueCode escapes tabs" {
            val field = StringField("test", "col1\tcol2")
            field.valueCode() shouldBe "\"col1\\tcol2\""
        }

        "StringField valueCode escapes dollar signs" {
            val field = StringField("test", "Cost: $50")
            field.valueCode() shouldBe "\"Cost: \\\$50\""
        }

        "StringField valueCode handles empty string" {
            val field = StringField("test", "")
            field.valueCode() shouldBe "\"\""
        }
        // endregion

        // region IntField
        "IntField valueCode returns integer without suffix" {
            val field = IntField("test", 42)
            field.valueCode() shouldBe "42"
        }

        "IntField valueCode handles negative values" {
            val field = IntField("test", -100)
            field.valueCode() shouldBe "-100"
        }

        "IntField valueCode handles zero" {
            val field = IntField("test", 0)
            field.valueCode() shouldBe "0"
        }
        // endregion

        // region LongField
        "LongField valueCode returns long with L suffix" {
            val field = LongField("test", 42L)
            field.valueCode() shouldBe "42L"
        }

        "LongField valueCode handles large values" {
            val field = LongField("test", 9223372036854775807L)
            field.valueCode() shouldBe "9223372036854775807L"
        }
        // endregion

        // region ByteField
        "ByteField valueCode returns byte without suffix" {
            val field = ByteField("test", 42.toByte())
            field.valueCode() shouldBe "42"
        }
        // endregion

        // region FloatField
        "FloatField valueCode returns float with f suffix" {
            val field = FloatField("test", 3.14f)
            field.valueCode() shouldBe "3.14f"
        }

        "FloatField valueCode handles NaN" {
            val field = FloatField("test", Float.NaN)
            field.valueCode() shouldBe "Float.NaN"
        }

        "FloatField valueCode handles positive infinity" {
            val field = FloatField("test", Float.POSITIVE_INFINITY)
            field.valueCode() shouldBe "Float.POSITIVE_INFINITY"
        }

        "FloatField valueCode handles negative infinity" {
            val field = FloatField("test", Float.NEGATIVE_INFINITY)
            field.valueCode() shouldBe "Float.NEGATIVE_INFINITY"
        }
        // endregion

        // region DoubleField
        "DoubleField valueCode returns double without d suffix" {
            val field = DoubleField("test", 3.14)
            field.valueCode() shouldBe "3.14"
        }

        "DoubleField valueCode handles NaN" {
            val field = DoubleField("test", Double.NaN)
            field.valueCode() shouldBe "Double.NaN"
        }

        "DoubleField valueCode handles positive infinity" {
            val field = DoubleField("test", Double.POSITIVE_INFINITY)
            field.valueCode() shouldBe "Double.POSITIVE_INFINITY"
        }

        "DoubleField valueCode handles negative infinity" {
            val field = DoubleField("test", Double.NEGATIVE_INFINITY)
            field.valueCode() shouldBe "Double.NEGATIVE_INFINITY"
        }
        // endregion

        // region DpField
        "DpField valueCode returns dp format" {
            val field = DpField("test", 16.dp)
            field.valueCode() shouldBe "16.0.dp"
        }
        // endregion

        // region SpField
        "SpField valueCode returns sp format" {
            val field = SpField("test", 14.sp)
            field.valueCode() shouldBe "14.0.sp"
        }
        // endregion

        // region OffsetField
        "OffsetField valueCode returns Offset constructor format" {
            val field = OffsetField("test", Offset(10f, 20f))
            field.valueCode() shouldBe "Offset(x = 10.0f, y = 20.0f)"
        }

        "OffsetField valueCode handles NaN values" {
            val field = OffsetField("test", Offset(Float.NaN, Float.NaN))
            field.valueCode() shouldBe "Offset(x = Float.NaN, y = Float.NaN)"
        }

        "OffsetField valueCode handles infinity values" {
            val field = OffsetField("test", Offset(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY))
            field.valueCode() shouldBe "Offset(x = Float.POSITIVE_INFINITY, y = Float.NEGATIVE_INFINITY)"
        }
        // endregion

        // region DpOffsetField
        "DpOffsetField valueCode returns DpOffset constructor format" {
            val field = DpOffsetField("test", DpOffset(10.dp, 20.dp))
            field.valueCode() shouldBe "DpOffset(x = 10.0.dp, y = 20.0.dp)"
        }
        // endregion

        // region SizeField
        "SizeField valueCode returns Size constructor format" {
            val field = SizeField("test", Size(100f, 200f))
            field.valueCode() shouldBe "Size(width = 100.0f, height = 200.0f)"
        }

        "SizeField valueCode handles NaN values" {
            val field = SizeField("test", Size(Float.NaN, Float.NaN))
            field.valueCode() shouldBe "Size(width = Float.NaN, height = Float.NaN)"
        }
        // endregion

        // region DpSizeField
        "DpSizeField valueCode returns DpSize constructor format" {
            val field = DpSizeField("test", DpSize(100.dp, 200.dp))
            field.valueCode() shouldBe "DpSize(width = 100.0.dp, height = 200.0.dp)"
        }
        // endregion

        // region ColorField
        "ColorField valueCode returns predefined color name for Red" {
            val field = ColorField("test", Color.Red)
            field.valueCode() shouldBe "Color.Red"
        }

        "ColorField valueCode returns predefined color name for Blue" {
            val field = ColorField("test", Color.Blue)
            field.valueCode() shouldBe "Color.Blue"
        }

        "ColorField valueCode returns predefined color name for Green" {
            val field = ColorField("test", Color.Green)
            field.valueCode() shouldBe "Color.Green"
        }

        "ColorField valueCode returns predefined color name for Black" {
            val field = ColorField("test", Color.Black)
            field.valueCode() shouldBe "Color.Black"
        }

        "ColorField valueCode returns predefined color name for White" {
            val field = ColorField("test", Color.White)
            field.valueCode() shouldBe "Color.White"
        }

        "ColorField valueCode returns predefined color name for Transparent" {
            val field = ColorField("test", Color.Transparent)
            field.valueCode() shouldBe "Color.Transparent"
        }

        "ColorField valueCode returns hex for custom opaque color" {
            val field = ColorField("test", Color(0xFF123456))
            field.valueCode() shouldBe "Color(0xFF123456)"
        }

        "ColorField valueCode returns hex with alpha for semi-transparent color" {
            val field = ColorField("test", Color(0x80123456))
            field.valueCode() shouldBe "Color(0x80123456)"
        }
        // endregion

        // region SelectableField
        "SelectableField valueCode uses custom valueCode function" {
            val field = SelectableField(
                label = "test",
                choices = listOf("A", "B", "C"),
                valueCode = { "\"$it\"" },
            )
            field.valueCode() shouldBe "\"A\""
        }

        "SelectableField valueCode returns default when no valueCode provided" {
            val field = SelectableField(
                label = "test",
                choices = listOf("A", "B", "C"),
            )
            field.valueCode() shouldBe "/* TODO Set test value here */"
        }
        // endregion

        // region NullableField
        "NullableField valueCode returns null for null value" {
            val field = StringField("test", "default").nullable()
            field.value = null
            field.valueCode() shouldBe "null"
        }

        "NullableField valueCode delegates to baseField for non-null value" {
            val field = StringField("test", "Hello").nullable()
            field.valueCode() shouldBe "\"Hello\""
        }
        // endregion

        // region WithTestValuesField
        "WithTestValuesField valueCode delegates to baseField" {
            val field = StringField("test", "Hello").withTestValues("World", "!")
            field.valueCode() shouldBe "\"Hello\""
        }
        // endregion

        // region WithHintField
        "WithHintField valueCode delegates to baseField" {
            val field = StringField("test", "Hello").withHint("Hint1" to "World")
            field.valueCode() shouldBe "\"Hello\""
        }
        // endregion

        // region WithValueCodeField
        "WithValueCodeField valueCode uses custom valueCode function" {
            val field = StringField("test", "Hello").withValueCode { "customCode($it)" }
            field.valueCode() shouldBe "customCode(Hello)"
        }
        // endregion

        // region TransformField
        "TransformField valueCode uses custom valueCode function" {
            val baseField = IntField("test", 100)
            val field = TransformField(
                baseField = baseField,
                transform = { it.toString() },
                reverse = { it.toInt() },
                valueCode = { "\"$it\"" },
            )
            field.valueCode() shouldBe "\"100\""
        }

        "TransformField valueCode returns default when no valueCode provided" {
            val baseField = IntField("test", 100)
            val field = TransformField(
                baseField = baseField,
                transform = { it.toString() },
                reverse = { it.toInt() },
            )
            field.valueCode() shouldBe "/* TODO Set test value here */"
        }
        // endregion

        // region Value change tests
        "valueCode reflects current value after change" {
            val field = IntField("test", 10)
            field.valueCode() shouldBe "10"
            field.value = 20
            field.valueCode() shouldBe "20"
        }

        "StringField valueCode reflects changed value with escaping" {
            val field = StringField("test", "initial")
            field.valueCode() shouldBe "\"initial\""
            field.value = "new\nvalue"
            field.valueCode() shouldBe "\"new\\nvalue\""
        }
        // endregion
    })
