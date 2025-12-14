package me.tbsten.compose.preview.lab.field

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

class ValueCodeTest {

    // region BooleanField
    @Test
    fun `BooleanField valueCode returns true for true value`() {
        val field = BooleanField("test", true)
        assertEquals("true", field.valueCode())
    }

    @Test
    fun `BooleanField valueCode returns false for false value`() {
        val field = BooleanField("test", false)
        assertEquals("false", field.valueCode())
    }
    // endregion

    // region StringField
    @Test
    fun `StringField valueCode returns quoted string`() {
        val field = StringField("test", "Hello")
        assertEquals("\"Hello\"", field.valueCode())
    }

    @Test
    fun `StringField valueCode escapes double quotes`() {
        val field = StringField("test", "Hello \"World\"")
        assertEquals("\"Hello \\\"World\\\"\"", field.valueCode())
    }

    @Test
    fun `StringField valueCode escapes backslashes`() {
        val field = StringField("test", "path\\to\\file")
        assertEquals("\"path\\\\to\\\\file\"", field.valueCode())
    }

    @Test
    fun `StringField valueCode escapes newlines`() {
        val field = StringField("test", "line1\nline2")
        assertEquals("\"line1\\nline2\"", field.valueCode())
    }

    @Test
    fun `StringField valueCode escapes carriage returns`() {
        val field = StringField("test", "line1\rline2")
        assertEquals("\"line1\\rline2\"", field.valueCode())
    }

    @Test
    fun `StringField valueCode escapes tabs`() {
        val field = StringField("test", "col1\tcol2")
        assertEquals("\"col1\\tcol2\"", field.valueCode())
    }

    @Test
    fun `StringField valueCode escapes dollar signs`() {
        val field = StringField("test", "Cost: $50")
        assertEquals("\"Cost: \\\$50\"", field.valueCode())
    }

    @Test
    fun `StringField valueCode handles empty string`() {
        val field = StringField("test", "")
        assertEquals("\"\"", field.valueCode())
    }
    // endregion

    // region IntField
    @Test
    fun `IntField valueCode returns integer without suffix`() {
        val field = IntField("test", 42)
        assertEquals("42", field.valueCode())
    }

    @Test
    fun `IntField valueCode handles negative values`() {
        val field = IntField("test", -100)
        assertEquals("-100", field.valueCode())
    }

    @Test
    fun `IntField valueCode handles zero`() {
        val field = IntField("test", 0)
        assertEquals("0", field.valueCode())
    }
    // endregion

    // region LongField
    @Test
    fun `LongField valueCode returns long with L suffix`() {
        val field = LongField("test", 42L)
        assertEquals("42L", field.valueCode())
    }

    @Test
    fun `LongField valueCode handles large values`() {
        val field = LongField("test", 9223372036854775807L)
        assertEquals("9223372036854775807L", field.valueCode())
    }
    // endregion

    // region ByteField
    @Test
    fun `ByteField valueCode returns byte without suffix`() {
        val field = ByteField("test", 42.toByte())
        assertEquals("42", field.valueCode())
    }
    // endregion

    // region FloatField
    @Test
    fun `FloatField valueCode returns float with f suffix`() {
        val field = FloatField("test", 3.14f)
        assertEquals("3.14f", field.valueCode())
    }

    @Test
    fun `FloatField valueCode handles NaN`() {
        val field = FloatField("test", Float.NaN)
        assertEquals("Float.NaN", field.valueCode())
    }

    @Test
    fun `FloatField valueCode handles positive infinity`() {
        val field = FloatField("test", Float.POSITIVE_INFINITY)
        assertEquals("Float.POSITIVE_INFINITY", field.valueCode())
    }

    @Test
    fun `FloatField valueCode handles negative infinity`() {
        val field = FloatField("test", Float.NEGATIVE_INFINITY)
        assertEquals("Float.NEGATIVE_INFINITY", field.valueCode())
    }
    // endregion

    // region DoubleField
    @Test
    fun `DoubleField valueCode returns double without d suffix`() {
        val field = DoubleField("test", 3.14)
        assertEquals("3.14", field.valueCode())
    }

    @Test
    fun `DoubleField valueCode handles NaN`() {
        val field = DoubleField("test", Double.NaN)
        assertEquals("Double.NaN", field.valueCode())
    }

    @Test
    fun `DoubleField valueCode handles positive infinity`() {
        val field = DoubleField("test", Double.POSITIVE_INFINITY)
        assertEquals("Double.POSITIVE_INFINITY", field.valueCode())
    }

    @Test
    fun `DoubleField valueCode handles negative infinity`() {
        val field = DoubleField("test", Double.NEGATIVE_INFINITY)
        assertEquals("Double.NEGATIVE_INFINITY", field.valueCode())
    }
    // endregion

    // region DpField
    @Test
    fun `DpField valueCode returns dp format`() {
        val field = DpField("test", 16.dp)
        assertEquals("16.0.dp", field.valueCode())
    }
    // endregion

    // region SpField
    @Test
    fun `SpField valueCode returns sp format`() {
        val field = SpField("test", 14.sp)
        assertEquals("14.0.sp", field.valueCode())
    }
    // endregion

    // region OffsetField
    @Test
    fun `OffsetField valueCode returns Offset constructor format`() {
        val field = OffsetField("test", Offset(10f, 20f))
        assertEquals("Offset(x = 10.0f, y = 20.0f)", field.valueCode())
    }

    @Test
    fun `OffsetField valueCode handles NaN values`() {
        val field = OffsetField("test", Offset(Float.NaN, Float.NaN))
        assertEquals("Offset(x = Float.NaN, y = Float.NaN)", field.valueCode())
    }

    @Test
    fun `OffsetField valueCode handles infinity values`() {
        val field = OffsetField("test", Offset(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY))
        assertEquals("Offset(x = Float.POSITIVE_INFINITY, y = Float.NEGATIVE_INFINITY)", field.valueCode())
    }
    // endregion

    // region DpOffsetField
    @Test
    fun `DpOffsetField valueCode returns DpOffset constructor format`() {
        val field = DpOffsetField("test", DpOffset(10.dp, 20.dp))
        assertEquals("DpOffset(x = 10.0.dp, y = 20.0.dp)", field.valueCode())
    }
    // endregion

    // region SizeField
    @Test
    fun `SizeField valueCode returns Size constructor format`() {
        val field = SizeField("test", Size(100f, 200f))
        assertEquals("Size(width = 100.0f, height = 200.0f)", field.valueCode())
    }

    @Test
    fun `SizeField valueCode handles NaN values`() {
        val field = SizeField("test", Size(Float.NaN, Float.NaN))
        assertEquals("Size(width = Float.NaN, height = Float.NaN)", field.valueCode())
    }
    // endregion

    // region DpSizeField
    @Test
    fun `DpSizeField valueCode returns DpSize constructor format`() {
        val field = DpSizeField("test", DpSize(100.dp, 200.dp))
        assertEquals("DpSize(width = 100.0.dp, height = 200.0.dp)", field.valueCode())
    }
    // endregion

    // region ColorField
    @Test
    fun `ColorField valueCode returns predefined color name for Red`() {
        val field = ColorField("test", Color.Red)
        assertEquals("Color.Red", field.valueCode())
    }

    @Test
    fun `ColorField valueCode returns predefined color name for Blue`() {
        val field = ColorField("test", Color.Blue)
        assertEquals("Color.Blue", field.valueCode())
    }

    @Test
    fun `ColorField valueCode returns predefined color name for Green`() {
        val field = ColorField("test", Color.Green)
        assertEquals("Color.Green", field.valueCode())
    }

    @Test
    fun `ColorField valueCode returns predefined color name for Black`() {
        val field = ColorField("test", Color.Black)
        assertEquals("Color.Black", field.valueCode())
    }

    @Test
    fun `ColorField valueCode returns predefined color name for White`() {
        val field = ColorField("test", Color.White)
        assertEquals("Color.White", field.valueCode())
    }

    @Test
    fun `ColorField valueCode returns predefined color name for Transparent`() {
        val field = ColorField("test", Color.Transparent)
        assertEquals("Color.Transparent", field.valueCode())
    }

    @Test
    fun `ColorField valueCode returns hex for custom opaque color`() {
        val field = ColorField("test", Color(0xFF123456))
        assertEquals("Color(0xFF123456)", field.valueCode())
    }

    @Test
    fun `ColorField valueCode returns hex with alpha for semi-transparent color`() {
        val field = ColorField("test", Color(0x80123456))
        assertEquals("Color(0x80123456)", field.valueCode())
    }
    // endregion

    // region SelectableField
    @Test
    fun `SelectableField valueCode uses custom valueCode function`() {
        val field = SelectableField(
            label = "test",
            choices = listOf("A", "B", "C"),
            valueCode = { "\"$it\"" },
        )
        assertEquals("\"A\"", field.valueCode())
    }

    @Test
    fun `SelectableField valueCode returns default when no valueCode provided`() {
        val field = SelectableField(
            label = "test",
            choices = listOf("A", "B", "C"),
        )
        assertEquals("/* TODO Set test value here */", field.valueCode())
    }
    // endregion

    // region NullableField
    @Test
    fun `NullableField valueCode returns null for null value`() {
        val field = StringField("test", "default").nullable()
        field.value = null
        assertEquals("null", field.valueCode())
    }

    @Test
    fun `NullableField valueCode delegates to baseField for non-null value`() {
        val field = StringField("test", "Hello").nullable()
        assertEquals("\"Hello\"", field.valueCode())
    }
    // endregion

    // region WithTestValuesField
    @Test
    fun `WithTestValuesField valueCode delegates to baseField`() {
        val field = StringField("test", "Hello").withTestValues("World", "!")
        assertEquals("\"Hello\"", field.valueCode())
    }
    // endregion

    // region WithHintField
    @Test
    fun `WithHintField valueCode delegates to baseField`() {
        val field = StringField("test", "Hello").withHint("Hint1" to "World")
        assertEquals("\"Hello\"", field.valueCode())
    }
    // endregion

    // region WithValueCodeField
    @Test
    fun `WithValueCodeField valueCode uses custom valueCode function`() {
        val field = StringField("test", "Hello").withValueCode { "customCode($it)" }
        assertEquals("customCode(Hello)", field.valueCode())
    }
    // endregion

    // region TransformField
    @Test
    fun `TransformField valueCode uses custom valueCode function`() {
        val baseField = IntField("test", 100)
        val field = TransformField(
            baseField = baseField,
            transform = { it.toString() },
            reverse = { it.toInt() },
            valueCode = { "\"$it\"" },
        )
        assertEquals("\"100\"", field.valueCode())
    }

    @Test
    fun `TransformField valueCode returns default when no valueCode provided`() {
        val baseField = IntField("test", 100)
        val field = TransformField(
            baseField = baseField,
            transform = { it.toString() },
            reverse = { it.toInt() },
        )
        assertEquals("/* TODO Set test value here */", field.valueCode())
    }
    // endregion

    // region Value change tests
    @Test
    fun `valueCode reflects current value after change`() {
        val field = IntField("test", 10)
        assertEquals("10", field.valueCode())
        field.value = 20
        assertEquals("20", field.valueCode())
    }

    @Test
    fun `StringField valueCode reflects changed value with escaping`() {
        val field = StringField("test", "initial")
        assertEquals("\"initial\"", field.valueCode())
        field.value = "new\nvalue"
        assertEquals("\"new\\nvalue\"", field.valueCode())
    }
    // endregion
}
