package me.tbsten.compose.preview.lab.field

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

/**
 * A field that combines multiple sub-fields into a single composite value.
 *
 * CombinedField allows creating complex field types by combining simpler field types.
 * For example, combining multiple number fields to create a coordinate field, or
 * combining color and size fields to create a styled element field.
 *
 * # Usage
 *
 * ```kotlin
 * // Custom data class with multiple fields
 * data class Padding(public val horizontal: Dp, public val vertical: Dp)
 *
 * @Preview
 * @Composable
 * fun CustomFieldPreview() = PreviewLab {
 *     val padding: Padding = fieldValue {
 *         CombinedField(
 *             label = "Padding",
 *             fields = listOf(
 *                 DpField("Horizontal", 16.dp),
 *                 DpField("Vertical", 8.dp)
 *             ),
 *             combine = { values -> Padding(values[0] as Dp, values[1] as Dp) },
 *             split = { listOf(it.horizontal, it.vertical) }
 *         )
 *     }
 *
 *     Box(
 *         modifier = Modifier
 *             .background(Color.LightGray)
 *             .padding(horizontal = padding.horizontal, vertical = padding.vertical)
 *     ) {
 *         Text("Content with custom padding")
 *     }
 * }
 * ```
 *
 * @param Base The base type of the individual sub-fields
 * @param Value The composite value type created by combining the sub-fields
 * @param label The display label for this combined field
 * @param fields The list of sub-fields to combine
 * @param combine Function to combine values from sub-fields into the composite value
 * @param split Function to split a composite value back into individual sub-field values
 */
public open class CombinedField<Base, Value>(
    label: String,
    private val fields: List<MutablePreviewLabField<out Base>>,
    private val combine: (List<Base>) -> Value,
    private val split: (Value) -> List<Base>,
) : MutablePreviewLabField<Value>(
    label = label,
    initialValue = combine(fields.map { it.value }),
) {
    override fun testValues(): List<Value> =
        super.testValues() + cartesianProduct(fields.map { it.testValues() }).map { combine(it) }

    private fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
        if (lists.isEmpty()) return listOf(emptyList())
        return lists.fold(listOf(emptyList())) { acc, list ->
            acc.flatMap { existing -> list.map { existing + it } }
        }
    }

    private val _value by derivedStateOf {
        combine(fields.map { it.value })
    }

    override var value: Value
        get() = _value
        set(value) {
            split(value).forEachIndexed { index, splitValue ->
                @Suppress("UNCHECKED_CAST")
                (fields[index] as MutablePreviewLabField<Any?>).value = splitValue
            }
        }

    @Composable
    override fun Content() {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            fields.forEachIndexed { index, field ->
                val no = index + 1
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "$no. ${field.label}",
                        style = PreviewLabTheme.typography.label1,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .weight(1f, fill = true),
                    )
                    Box(modifier = Modifier.weight(3f, fill = false)) {
                        field.Content()
                    }
                }
            }
        }
    }
}

// CombinedField1

/**
 * Data class that holds a single value split from a combined field.
 *
 * @param A Type of the value
 * @param first The split value
 */
public data class Splited1<A>(public val first: A)

/**
 * Creates a [Splited1] instance containing a single value.
 *
 * @param A Type of the value
 * @param first The value
 * @return A new [Splited1] instance
 */
public fun <A> splitedOf(first: A): Splited1<A> = Splited1(first)

public open class CombinedField1<A, Value>(
    label: String,
    field1: MutablePreviewLabField<A>,
    private val combine: (A) -> Value,
    private val split: (Value) -> Splited1<A>,
) : CombinedField<Any?, Value>(
    label = label,
    fields = listOf(field1),
    combine = {
        @Suppress("UNCHECKED_CAST")
        combine(it[0] as A)
    },
    split = { v ->
        val s = split(v)
        listOf(s.first)
    },
)

/**
 * Creates a combined field from a single sub-field with transformation.
 *
 * # Usage
 *
 * ```kotlin
 * // Wrapping a single field with transformation
 * data class UserId(public val value: String)
 *
 * @Preview
 * @Composable
 * fun UserIdFieldPreview() = PreviewLab {
 *     val userId: UserId = fieldValue {
 *         combined(
 *             label = "User ID",
 *             field1 = StringField("ID", "user-001"),
 *             combine = { id -> UserId(id) },
 *             split = { splitedOf(it.value) }
 *         )
 *     }
 *
 *     Text("User ID: ${userId.value}")
 * }
 * ```
 *
 * @param label Display label for the combined field
 * @param field1 The sub-field
 * @param combine Function to transform the field value into the composite value
 * @param split Function to extract the field value from the composite value
 */
public fun <A, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    combine: (A) -> Value,
    split: (Value) -> Splited1<A>,
): CombinedField1<A, Value> = CombinedField1(
    label = label,
    field1 = field1,
    combine = combine,
    split = split,
)

// CombinedField2

/**
 * Data class that holds two values split from a combined field.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param first The first split value
 * @param second The second split value
 */
public data class Splited2<A, B>(public val first: A, public val second: B)

/**
 * Creates a [Splited2] instance containing two values.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param first The first value
 * @param second The second value
 * @return A new [Splited2] instance
 */
public fun <A, B> splitedOf(first: A, second: B): Splited2<A, B> = Splited2(first, second)

public open class CombinedField2<A, B, Value>(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    private val combine: (A, B) -> Value,
    private val split: (Value) -> Splited2<A, B>,
) : CombinedField<Any?, Value>(
    label = label,
    fields = listOf(field1, field2),
    combine = {
        @Suppress("UNCHECKED_CAST")
        combine(it[0] as A, it[1] as B)
    },
    split = { v ->
        val s = split(v)
        listOf(s.first, s.second)
    },
)

/**
 * Creates a combined field from two sub-fields.
 *
 * # Usage
 *
 * ```kotlin
 * // Point with x and y coordinates
 * data class Point(public val x: Float, public val y: Float)
 *
 * @Preview
 * @Composable
 * fun PointFieldPreview() = PreviewLab {
 *     val point: Point = fieldValue {
 *         combined(
 *             label = "Point",
 *             field1 = FloatField("x", 10f),
 *             field2 = FloatField("y", 20f),
 *             combine = { x, y -> Point(x, y) },
 *             split = { splitedOf(it.x, it.y) }
 *         )
 *     }
 *
 *     Canvas(modifier = Modifier.size(200.dp)) {
 *         drawCircle(Color.Red, radius = 10f, center = Offset(point.x, point.y))
 *     }
 * }
 *
 * // Range with min and max
 * data class Range(public val min: Int, public val max: Int)
 *
 * @Preview
 * @Composable
 * fun RangeFieldPreview() = PreviewLab {
 *     val range: Range = fieldValue {
 *         combined(
 *             label = "Range",
 *             field1 = IntField("Min", 0),
 *             field2 = IntField("Max", 100),
 *             combine = { min, max -> Range(min, max) },
 *             split = { splitedOf(it.min, it.max) }
 *         )
 *     }
 *
 *     Text("Range: ${range.min} to ${range.max}")
 * }
 * ```
 *
 * @param label Display label for the combined field
 * @param field1 First sub-field
 * @param field2 Second sub-field
 * @param combine Function to combine two values into the composite value
 * @param split Function to split the composite value back into two values
 */
public fun <A, B, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    combine: (A, B) -> Value,
    split: (Value) -> Splited2<A, B>,
): CombinedField2<A, B, Value> = CombinedField2(
    label = label,
    field1 = field1,
    field2 = field2,
    combine = combine,
    split = split,
)

// CombinedField3

/**
 * Data class that holds three values split from a combined field.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param first The first split value
 * @param second The second split value
 * @param third The third split value
 */
public data class Splited3<A, B, C>(public val first: A, public val second: B, public val third: C)

/**
 * Creates a [Splited3] instance containing three values.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param first The first value
 * @param second The second value
 * @param third The third value
 * @return A new [Splited3] instance
 */
public fun <A, B, C> splitedOf(first: A, second: B, third: C): Splited3<A, B, C> = Splited3(first, second, third)

public open class CombinedField3<A, B, C, Value>(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    private val combine: (A, B, C) -> Value,
    private val split: (Value) -> Splited3<A, B, C>,
) : CombinedField<Any?, Value>(
    label = label,
    fields = listOf(field1, field2, field3),
    combine = {
        @Suppress("UNCHECKED_CAST")
        combine(it[0] as A, it[1] as B, it[2] as C)
    },
    split = {
        split(it).let { listOf(it.first, it.second, it.third) }
    },
)

/**
 * Creates a combined field from three sub-fields.
 *
 * # Usage
 *
 * ```kotlin
 * // RGB color from three Int values
 * data class RgbColor(public val red: Int, public val green: Int, public val blue: Int)
 *
 * @Preview
 * @Composable
 * fun RgbFieldPreview() = PreviewLab {
 *     val rgb: RgbColor = fieldValue {
 *         combined(
 *             label = "RGB Color",
 *             field1 = IntField("Red", 255),
 *             field2 = IntField("Green", 0),
 *             field3 = IntField("Blue", 0),
 *             combine = { r, g, b -> RgbColor(r, g, b) },
 *             split = { splitedOf(it.red, it.green, it.blue) }
 *         )
 *     }
 *
 *     Box(
 *         modifier = Modifier
 *             .size(100.dp)
 *             .background(Color(rgb.red, rgb.green, rgb.blue))
 *     )
 * }
 *
 * // 3D Point
 * data class Point3D(public val x: Float, public val y: Float, public val z: Float)
 *
 * @Preview
 * @Composable
 * fun Point3DFieldPreview() = PreviewLab {
 *     val point: Point3D = fieldValue {
 *         combined(
 *             label = "3D Point",
 *             field1 = FloatField("X", 0f),
 *             field2 = FloatField("Y", 0f),
 *             field3 = FloatField("Z", 0f),
 *             combine = { x, y, z -> Point3D(x, y, z) },
 *             split = { splitedOf(it.x, it.y, it.z) }
 *         )
 *     }
 *
 *     Text("Point: (${point.x}, ${point.y}, ${point.z})")
 * }
 * ```
 *
 * @param label Display label for the combined field
 * @param field1 First sub-field
 * @param field2 Second sub-field
 * @param field3 Third sub-field
 * @param combine Function to combine three values into the composite value
 * @param split Function to split the composite value back into three values
 */
public fun <A, B, C, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    combine: (A, B, C) -> Value,
    split: (Value) -> Splited3<A, B, C>,
): CombinedField3<A, B, C, Value> = CombinedField3(
    label = label,
    field1 = field1,
    field2 = field2,
    field3 = field3,
    combine = combine,
    split = split,
)

// CombinedField4

/**
 * Data class that holds four values split from a combined field.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param first The first split value
 * @param second The second split value
 * @param third The third split value
 * @param fourth The fourth split value
 */
public data class Splited4<A, B, C, D>(public val first: A, public val second: B, public val third: C, public val fourth: D)

/**
 * Creates a [Splited4] instance containing four values.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param first The first value
 * @param second The second value
 * @param third The third value
 * @param fourth The fourth value
 * @return A new [Splited4] instance
 */
public fun <A, B, C, D> splitedOf(first: A, second: B, third: C, fourth: D): Splited4<A, B, C, D> =
    Splited4(first, second, third, fourth)

public open class CombinedField4<A, B, C, D, Value>(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    private val combine: (A, B, C, D) -> Value,
    private val split: (Value) -> Splited4<A, B, C, D>,
) : CombinedField<Any?, Value>(
    label = label,
    fields = listOf(field1, field2, field3, field4),
    combine = {
        @Suppress("UNCHECKED_CAST")
        combine(it[0] as A, it[1] as B, it[2] as C, it[3] as D)
    },
    split = { v ->
        val s = split(v)
        listOf(s.first, s.second, s.third, s.fourth)
    },
)

/**
 * Creates a combined field from four sub-fields.
 *
 * # Usage
 *
 * ```kotlin
 * // RGBA color with alpha channel
 * data class RgbaColor(public val red: Int, public val green: Int, public val blue: Int, public val alpha: Float)
 *
 * @Preview
 * @Composable
 * fun RgbaFieldPreview() = PreviewLab {
 *     val rgba: RgbaColor = fieldValue {
 *         combined(
 *             label = "RGBA Color",
 *             field1 = IntField("Red", 255),
 *             field2 = IntField("Green", 0),
 *             field3 = IntField("Blue", 0),
 *             field4 = FloatField("Alpha", 0.5f),
 *             combine = { r, g, b, a -> RgbaColor(r, g, b, a) },
 *             split = { splitedOf(it.red, it.green, it.blue, it.alpha) }
 *         )
 *     }
 *
 *     Box(
 *         modifier = Modifier
 *             .size(100.dp)
 *             .background(Color(rgba.red, rgba.green, rgba.blue, (rgba.alpha * 255).toInt()))
 *     )
 * }
 *
 * // Rectangle with position and size
 * data class Rectangle(public val x: Dp, public val y: Dp, public val width: Dp, public val height: Dp)
 *
 * @Preview
 * @Composable
 * fun RectangleFieldPreview() = PreviewLab {
 *     val rect: Rectangle = fieldValue {
 *         combined(
 *             label = "Rectangle",
 *             field1 = DpField("X", 10.dp),
 *             field2 = DpField("Y", 10.dp),
 *             field3 = DpField("Width", 100.dp),
 *             field4 = DpField("Height", 50.dp),
 *             combine = { x, y, w, h -> Rectangle(x, y, w, h) },
 *             split = { splitedOf(it.x, it.y, it.width, it.height) }
 *         )
 *     }
 *
 *     Box(modifier = Modifier.size(200.dp)) {
 *         Box(
 *             modifier = Modifier
 *                 .offset(rect.x, rect.y)
 *                 .size(rect.width, rect.height)
 *                 .background(Color.Blue)
 *         )
 *     }
 * }
 * ```
 *
 * @param label Display label for the combined field
 * @param field1 First sub-field
 * @param field2 Second sub-field
 * @param field3 Third sub-field
 * @param field4 Fourth sub-field
 * @param combine Function to combine four values into the composite value
 * @param split Function to split the composite value back into four values
 */
public fun <A, B, C, D, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    combine: (A, B, C, D) -> Value,
    split: (Value) -> Splited4<A, B, C, D>,
): CombinedField4<A, B, C, D, Value> = CombinedField4(
    label = label,
    field1 = field1,
    field2 = field2,
    field3 = field3,
    field4 = field4,
    combine = combine,
    split = split,
)

// CombinedField5

/**
 * Data class that holds five values split from a combined field.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param first The first split value
 * @param second The second split value
 * @param third The third split value
 * @param fourth The fourth split value
 * @param fifth The fifth split value
 */
public data class Splited5<A, B, C, D, E>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val fourth: D,
    public val fifth: E
)

/**
 * Creates a [Splited5] instance containing five values.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param first The first value
 * @param second The second value
 * @param third The third value
 * @param fourth The fourth value
 * @param fifth The fifth value
 * @return A new [Splited5] instance
 */
public fun <A, B, C, D, E> splitedOf(first: A, second: B, third: C, fourth: D, fifth: E): Splited5<A, B, C, D, E> =
    Splited5(first, second, third, fourth, fifth)

public open class CombinedField5<A, B, C, D, E, Value>(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    private val combine: (A, B, C, D, E) -> Value,
    private val split: (Value) -> Splited5<A, B, C, D, E>,
) : CombinedField<Any?, Value>(
    label = label,
    fields = listOf(field1, field2, field3, field4, field5),
    combine = {
        @Suppress("UNCHECKED_CAST")
        combine(it[0] as A, it[1] as B, it[2] as C, it[3] as D, it[4] as E)
    },
    split = { v ->
        val s = split(v)
        listOf(s.first, s.second, s.third, s.fourth, s.fifth)
    },
)

/**
 * Creates a combined field from five sub-fields.
 *
 * This function allows combining five independent field values into a single composite value,
 * useful for creating complex data structures with multiple properties.
 *
 * # Usage
 *
 * ```kotlin
 * // Address with multiple components
 * data class Address(
 *     val street: String,
 *     val city: String,
 *     val state: String,
 *     val zipCode: String,
 *     val country: String
 * )
 *
 * @Preview
 * @Composable
 * fun AddressFieldPreview() = PreviewLab {
 *     val address: Address = fieldValue {
 *         combined(
 *             label = "Address",
 *             field1 = StringField("Street", "123 Main St"),
 *             field2 = StringField("City", "New York"),
 *             field3 = StringField("State", "NY"),
 *             field4 = StringField("Zip Code", "10001"),
 *             field5 = StringField("Country", "USA"),
 *             combine = { street, city, state, zip, country ->
 *                 Address(street, city, state, zip, country)
 *             },
 *             split = { splitedOf(it.street, it.city, it.state, it.zipCode, it.country) }
 *         )
 *     }
 *
 *     Column {
 *         Text(address.street)
 *         Text("${address.city}, ${address.state} ${address.zipCode}")
 *         Text(address.country)
 *     }
 * }
 * ```
 *
 * @param A Type of the first field value
 * @param B Type of the second field value
 * @param C Type of the third field value
 * @param D Type of the fourth field value
 * @param E Type of the fifth field value
 * @param Value The composite value type created by combining all five field values
 * @param label Display label for the combined field
 * @param field1 First sub-field
 * @param field2 Second sub-field
 * @param field3 Third sub-field
 * @param field4 Fourth sub-field
 * @param field5 Fifth sub-field
 * @param combine Function to combine five values into the composite value
 * @param split Function to split the composite value back into five values
 * @return A new [CombinedField5] instance that manages the five sub-fields as a single composite value
 */
public fun <A, B, C, D, E, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    combine: (A, B, C, D, E) -> Value,
    split: (Value) -> Splited5<A, B, C, D, E>,
): CombinedField5<A, B, C, D, E, Value> = CombinedField5(
    label = label,
    field1 = field1,
    field2 = field2,
    field3 = field3,
    field4 = field4,
    field5 = field5,
    combine = combine,
    split = split,
)

// CombinedField6

/**
 * Data class that holds six values split from a combined field.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param F Type of the sixth value
 * @param first The first split value
 * @param second The second split value
 * @param third The third split value
 * @param fourth The fourth split value
 * @param fifth The fifth split value
 * @param sixth The sixth split value
 */
public data class Splited6<A, B, C, D, E, F>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val fourth: D,
    public val fifth: E,
    public val sixth: F
)

/**
 * Creates a [Splited6] instance containing six values.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param F Type of the sixth value
 * @param first The first value
 * @param second The second value
 * @param third The third value
 * @param fourth The fourth value
 * @param fifth The fifth value
 * @param sixth The sixth value
 * @return A new [Splited6] instance
 */
public fun <A, B, C, D, E, F> splitedOf(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
    sixth: F
): Splited6<A, B, C, D, E, F> = Splited6(first, second, third, fourth, fifth, sixth)

public open class CombinedField6<A, B, C, D, E, F, Value>(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    private val combine: (A, B, C, D, E, F) -> Value,
    private val split: (Value) -> Splited6<A, B, C, D, E, F>,
) : CombinedField<Any?, Value>(
    label = label,
    fields = listOf(field1, field2, field3, field4, field5, field6),
    combine = {
        @Suppress("UNCHECKED_CAST")
        combine(
            it[0] as A,
            it[1] as B,
            it[2] as C,
            it[3] as D,
            it[4] as E,
            it[5] as F,
        )
    },
    split = { v ->
        val s = split(v)
        listOf(s.first, s.second, s.third, s.fourth, s.fifth, s.sixth)
    },
)

/**
 * Creates a combined field from six sub-fields.
 *
 * This function allows combining six independent field values into a single composite value,
 * ideal for modeling entities with multiple attributes or configuration objects.
 *
 * # Usage
 *
 * ```kotlin
 * // Box with position, size, and appearance
 * data class BoxStyle(
 *     val x: Dp,
 *     val y: Dp,
 *     val width: Dp,
 *     val height: Dp,
 *     val color: Color,
 *     val alpha: Float
 * )
 *
 * @Preview
 * @Composable
 * fun BoxStyleFieldPreview() = PreviewLab {
 *     val boxStyle: BoxStyle = fieldValue {
 *         combined(
 *             label = "Box Style",
 *             field1 = DpField("X", 20.dp),
 *             field2 = DpField("Y", 20.dp),
 *             field3 = DpField("Width", 100.dp),
 *             field4 = DpField("Height", 100.dp),
 *             field5 = ColorField("Color", Color.Blue),
 *             field6 = FloatField("Alpha", 1f),
 *             combine = { x, y, w, h, color, alpha ->
 *                 BoxStyle(x, y, w, h, color, alpha)
 *             },
 *             split = { splitedOf(it.x, it.y, it.width, it.height, it.color, it.alpha) }
 *         )
 *     }
 *
 *     Box(modifier = Modifier.size(200.dp)) {
 *         Box(
 *             modifier = Modifier
 *                 .offset(boxStyle.x, boxStyle.y)
 *                 .size(boxStyle.width, boxStyle.height)
 *                 .background(boxStyle.color.copy(alpha = boxStyle.alpha))
 *         )
 *     }
 * }
 * ```
 *
 * @param A Type of the first field value
 * @param B Type of the second field value
 * @param C Type of the third field value
 * @param D Type of the fourth field value
 * @param E Type of the fifth field value
 * @param F Type of the sixth field value
 * @param Value The composite value type created by combining all six field values
 * @param label Display label for the combined field
 * @param field1 First sub-field
 * @param field2 Second sub-field
 * @param field3 Third sub-field
 * @param field4 Fourth sub-field
 * @param field5 Fifth sub-field
 * @param field6 Sixth sub-field
 * @param combine Function to combine six values into the composite value
 * @param split Function to split the composite value back into six values
 * @return A new [CombinedField6] instance that manages the six sub-fields as a single composite value
 */
public fun <A, B, C, D, E, F, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    combine: (A, B, C, D, E, F) -> Value,
    split: (Value) -> Splited6<A, B, C, D, E, F>,
): CombinedField6<A, B, C, D, E, F, Value> = CombinedField6(
    label = label,
    field1 = field1,
    field2 = field2,
    field3 = field3,
    field4 = field4,
    field5 = field5,
    field6 = field6,
    combine = combine,
    split = split,
)

// CombinedField7

/**
 * Data class that holds seven values split from a combined field.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param F Type of the sixth value
 * @param G Type of the seventh value
 * @param first The first split value
 * @param second The second split value
 * @param third The third split value
 * @param fourth The fourth split value
 * @param fifth The fifth split value
 * @param sixth The sixth split value
 * @param seventh The seventh split value
 */
public data class Splited7<A, B, C, D, E, F, G>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val fourth: D,
    public val fifth: E,
    public val sixth: F,
    public val seventh: G,
)

/**
 * Creates a [Splited7] instance containing seven values.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param F Type of the sixth value
 * @param G Type of the seventh value
 * @param first The first value
 * @param second The second value
 * @param third The third value
 * @param fourth The fourth value
 * @param fifth The fifth value
 * @param sixth The sixth value
 * @param seventh The seventh value
 * @return A new [Splited7] instance
 */
public fun <A, B, C, D, E, F, G> splitedOf(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
    sixth: F,
    seventh: G
): Splited7<A, B, C, D, E, F, G> = Splited7(first, second, third, fourth, fifth, sixth, seventh)

public open class CombinedField7<A, B, C, D, E, F, G, Value>(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    field7: MutablePreviewLabField<G>,
    private val combine: (A, B, C, D, E, F, G) -> Value,
    private val split: (Value) -> Splited7<A, B, C, D, E, F, G>,
) : CombinedField<Any?, Value>(
    label = label,
    fields = listOf(field1, field2, field3, field4, field5, field6, field7),
    combine = {
        @Suppress("UNCHECKED_CAST")
        combine(
            it[0] as A,
            it[1] as B,
            it[2] as C,
            it[3] as D,
            it[4] as E,
            it[5] as F,
            it[6] as G,
        )
    },
    split = { v ->
        val s = split(v)
        listOf(s.first, s.second, s.third, s.fourth, s.fifth, s.sixth, s.seventh)
    },
)

/**
 * Creates a combined field from seven sub-fields.
 *
 * This function allows combining seven independent field values into a single composite value,
 * suitable for complex entities with numerous configurable attributes.
 *
 * # Usage
 *
 * ```kotlin
 * // Text style configuration
 * data class TextStyle(
 *     val text: String,
 *     val fontSize: TextUnit,
 *     val fontWeight: FontWeight,
 *     val color: Color,
 *     val letterSpacing: TextUnit,
 *     val lineHeight: TextUnit,
 *     val textAlign: TextAlign
 * )
 *
 * @Preview
 * @Composable
 * fun TextStyleFieldPreview() = PreviewLab {
 *     val textStyle: TextStyle = fieldValue {
 *         combined(
 *             label = "Text Style",
 *             field1 = StringField("Text", "Hello World"),
 *             field2 = TextUnitField("Font Size", 16.sp),
 *             field3 = EnumField("Font Weight", FontWeight.Normal),
 *             field4 = ColorField("Color", Color.Black),
 *             field5 = TextUnitField("Letter Spacing", 0.sp),
 *             field6 = TextUnitField("Line Height", 20.sp),
 *             field7 = EnumField("Text Align", TextAlign.Start),
 *             combine = { text, size, weight, color, spacing, height, align ->
 *                 TextStyle(text, size, weight, color, spacing, height, align)
 *             },
 *             split = { splitedOf(it.text, it.fontSize, it.fontWeight, it.color, it.letterSpacing, it.lineHeight, it.textAlign) }
 *         )
 *     }
 *
 *     Text(
 *         text = textStyle.text,
 *         fontSize = textStyle.fontSize,
 *         fontWeight = textStyle.fontWeight,
 *         color = textStyle.color,
 *         letterSpacing = textStyle.letterSpacing,
 *         lineHeight = textStyle.lineHeight,
 *         textAlign = textStyle.textAlign
 *     )
 * }
 * ```
 *
 * @param A Type of the first field value
 * @param B Type of the second field value
 * @param C Type of the third field value
 * @param D Type of the fourth field value
 * @param E Type of the fifth field value
 * @param F Type of the sixth field value
 * @param G Type of the seventh field value
 * @param Value The composite value type created by combining all seven field values
 * @param label Display label for the combined field
 * @param field1 First sub-field
 * @param field2 Second sub-field
 * @param field3 Third sub-field
 * @param field4 Fourth sub-field
 * @param field5 Fifth sub-field
 * @param field6 Sixth sub-field
 * @param field7 Seventh sub-field
 * @param combine Function to combine seven values into the composite value
 * @param split Function to split the composite value back into seven values
 * @return A new [CombinedField7] instance that manages the seven sub-fields as a single composite value
 */
public fun <A, B, C, D, E, F, G, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    field7: MutablePreviewLabField<G>,
    combine: (A, B, C, D, E, F, G) -> Value,
    split: (Value) -> Splited7<A, B, C, D, E, F, G>,
): CombinedField7<A, B, C, D, E, F, G, Value> = CombinedField7(
    label = label,
    field1 = field1,
    field2 = field2,
    field3 = field3,
    field4 = field4,
    field5 = field5,
    field6 = field6,
    field7 = field7,
    combine = combine,
    split = split,
)

// CombinedField8

/**
 * Data class that holds eight values split from a combined field.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param F Type of the sixth value
 * @param G Type of the seventh value
 * @param H Type of the eighth value
 * @param first The first split value
 * @param second The second split value
 * @param third The third split value
 * @param fourth The fourth split value
 * @param fifth The fifth split value
 * @param sixth The sixth split value
 * @param seventh The seventh split value
 * @param eighth The eighth split value
 */
public data class Splited8<A, B, C, D, E, F, G, H>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val fourth: D,
    public val fifth: E,
    public val sixth: F,
    public val seventh: G,
    public val eighth: H,
)

/**
 * Creates a [Splited8] instance containing eight values.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param F Type of the sixth value
 * @param G Type of the seventh value
 * @param H Type of the eighth value
 * @param first The first value
 * @param second The second value
 * @param third The third value
 * @param fourth The fourth value
 * @param fifth The fifth value
 * @param sixth The sixth value
 * @param seventh The seventh value
 * @param eighth The eighth value
 * @return A new [Splited8] instance
 */
public fun <A, B, C, D, E, F, G, H> splitedOf(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
    sixth: F,
    seventh: G,
    eighth: H
): Splited8<A, B, C, D, E, F, G, H> = Splited8(first, second, third, fourth, fifth, sixth, seventh, eighth)

public open class CombinedField8<A, B, C, D, E, F, G, H, Value>(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    field7: MutablePreviewLabField<G>,
    field8: MutablePreviewLabField<H>,
    private val combine: (A, B, C, D, E, F, G, H) -> Value,
    private val split: (Value) -> Splited8<A, B, C, D, E, F, G, H>,
) : CombinedField<Any?, Value>(
    label = label,
    fields = listOf(field1, field2, field3, field4, field5, field6, field7, field8),
    combine = {
        @Suppress("UNCHECKED_CAST")
        combine(
            it[0] as A,
            it[1] as B,
            it[2] as C,
            it[3] as D,
            it[4] as E,
            it[5] as F,
            it[6] as G,
            it[7] as H,
        )
    },
    split = { v ->
        val s = split(v)
        listOf(
            s.first,
            s.second,
            s.third,
            s.fourth,
            s.fifth,
            s.sixth,
            s.seventh,
            s.eighth,
        )
    },
)

/**
 * Creates a combined field from eight sub-fields.
 *
 * This function allows combining eight independent field values into a single composite value,
 * perfect for highly configurable components or complex data structures.
 *
 * # Usage
 *
 * ```kotlin
 * // Button configuration with comprehensive styling
 * data class ButtonConfig(
 *     val text: String,
 *     val width: Dp,
 *     val height: Dp,
 *     val backgroundColor: Color,
 *     val textColor: Color,
 *     val cornerRadius: Dp,
 *     val elevation: Dp,
 *     val enabled: Boolean
 * )
 *
 * @Preview
 * @Composable
 * fun ButtonConfigFieldPreview() = PreviewLab {
 *     val config: ButtonConfig = fieldValue {
 *         combined(
 *             label = "Button Config",
 *             field1 = StringField("Text", "Click Me"),
 *             field2 = DpField("Width", 200.dp),
 *             field3 = DpField("Height", 48.dp),
 *             field4 = ColorField("Background", Color.Blue),
 *             field5 = ColorField("Text Color", Color.White),
 *             field6 = DpField("Corner Radius", 8.dp),
 *             field7 = DpField("Elevation", 4.dp),
 *             field8 = BooleanField("Enabled", true),
 *             combine = { text, w, h, bgColor, txtColor, radius, elev, enabled ->
 *                 ButtonConfig(text, w, h, bgColor, txtColor, radius, elev, enabled)
 *             },
 *             split = { splitedOf(it.text, it.width, it.height, it.backgroundColor, it.textColor, it.cornerRadius, it.elevation, it.enabled) }
 *         )
 *     }
 *
 *     Button(
 *         onClick = {},
 *         enabled = config.enabled,
 *         modifier = Modifier
 *             .size(config.width, config.height)
 *             .shadow(config.elevation, shape = RoundedCornerShape(config.cornerRadius)),
 *         colors = ButtonDefaults.buttonColors(containerColor = config.backgroundColor)
 *     ) {
 *         Text(config.text, color = config.textColor)
 *     }
 * }
 * ```
 *
 * @param A Type of the first field value
 * @param B Type of the second field value
 * @param C Type of the third field value
 * @param D Type of the fourth field value
 * @param E Type of the fifth field value
 * @param F Type of the sixth field value
 * @param G Type of the seventh field value
 * @param H Type of the eighth field value
 * @param Value The composite value type created by combining all eight field values
 * @param label Display label for the combined field
 * @param field1 First sub-field
 * @param field2 Second sub-field
 * @param field3 Third sub-field
 * @param field4 Fourth sub-field
 * @param field5 Fifth sub-field
 * @param field6 Sixth sub-field
 * @param field7 Seventh sub-field
 * @param field8 Eighth sub-field
 * @param combine Function to combine eight values into the composite value
 * @param split Function to split the composite value back into eight values
 * @return A new [CombinedField8] instance that manages the eight sub-fields as a single composite value
 */
public fun <A, B, C, D, E, F, G, H, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    field7: MutablePreviewLabField<G>,
    field8: MutablePreviewLabField<H>,
    combine: (A, B, C, D, E, F, G, H) -> Value,
    split: (Value) -> Splited8<A, B, C, D, E, F, G, H>,
): CombinedField8<A, B, C, D, E, F, G, H, Value> = CombinedField8(
    label = label,
    field1 = field1,
    field2 = field2,
    field3 = field3,
    field4 = field4,
    field5 = field5,
    field6 = field6,
    field7 = field7,
    field8 = field8,
    combine = combine,
    split = split,
)

// CombinedField9

/**
 * Data class that holds nine values split from a combined field.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param F Type of the sixth value
 * @param G Type of the seventh value
 * @param H Type of the eighth value
 * @param I Type of the ninth value
 * @param first The first split value
 * @param second The second split value
 * @param third The third split value
 * @param fourth The fourth split value
 * @param fifth The fifth split value
 * @param sixth The sixth split value
 * @param seventh The seventh split value
 * @param eighth The eighth split value
 * @param ninth The ninth split value
 */
public data class Splited9<A, B, C, D, E, F, G, H, I>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val fourth: D,
    public val fifth: E,
    public val sixth: F,
    public val seventh: G,
    public val eighth: H,
    public val ninth: I,
)

/**
 * Creates a [Splited9] instance containing nine values.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param F Type of the sixth value
 * @param G Type of the seventh value
 * @param H Type of the eighth value
 * @param I Type of the ninth value
 * @param first The first value
 * @param second The second value
 * @param third The third value
 * @param fourth The fourth value
 * @param fifth The fifth value
 * @param sixth The sixth value
 * @param seventh The seventh value
 * @param eighth The eighth value
 * @param ninth The ninth value
 * @return A new [Splited9] instance
 */
public fun <A, B, C, D, E, F, G, H, I> splitedOf(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
    sixth: F,
    seventh: G,
    eighth: H,
    ninth: I,
): Splited9<A, B, C, D, E, F, G, H, I> = Splited9(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth)

public open class CombinedField9<A, B, C, D, E, F, G, H, I, Value>(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    field7: MutablePreviewLabField<G>,
    field8: MutablePreviewLabField<H>,
    field9: MutablePreviewLabField<I>,
    private val combine: (A, B, C, D, E, F, G, H, I) -> Value,
    private val split: (Value) -> Splited9<A, B, C, D, E, F, G, H, I>,
) : CombinedField<Any?, Value>(
    label = label,
    fields = listOf(field1, field2, field3, field4, field5, field6, field7, field8, field9),
    combine = {
        @Suppress("UNCHECKED_CAST")
        combine(
            it[0] as A, it[1] as B, it[2] as C, it[3] as D, it[4] as E,
            it[5] as F, it[6] as G, it[7] as H, it[8] as I,
        )
    },
    split = { v ->
        val s = split(v)
        listOf(
            s.first, s.second, s.third, s.fourth, s.fifth,
            s.sixth, s.seventh, s.eighth, s.ninth,
        )
    },
)

/**
 * Creates a combined field from nine sub-fields.
 *
 * This function allows combining nine independent field values into a single composite value,
 * enabling fine-grained control over highly detailed configurations or multi-faceted data models.
 *
 * # Usage
 *
 * ```kotlin
 * // Card component with extensive customization
 * data class CardStyle(
 *     val title: String,
 *     val width: Dp,
 *     val height: Dp,
 *     val backgroundColor: Color,
 *     val borderColor: Color,
 *     val borderWidth: Dp,
 *     val cornerRadius: Dp,
 *     val elevation: Dp,
 *     val padding: Dp
 * )
 *
 * @Preview
 * @Composable
 * fun CardStyleFieldPreview() = PreviewLab {
 *     val cardStyle: CardStyle = fieldValue {
 *         combined(
 *             label = "Card Style",
 *             field1 = StringField("Title", "My Card"),
 *             field2 = DpField("Width", 300.dp),
 *             field3 = DpField("Height", 200.dp),
 *             field4 = ColorField("Background", Color.White),
 *             field5 = ColorField("Border", Color.Gray),
 *             field6 = DpField("Border Width", 1.dp),
 *             field7 = DpField("Corner Radius", 12.dp),
 *             field8 = DpField("Elevation", 4.dp),
 *             field9 = DpField("Padding", 16.dp),
 *             combine = { title, w, h, bg, border, bw, radius, elev, pad ->
 *                 CardStyle(title, w, h, bg, border, bw, radius, elev, pad)
 *             },
 *             split = { splitedOf(it.title, it.width, it.height, it.backgroundColor, it.borderColor, it.borderWidth, it.cornerRadius, it.elevation, it.padding) }
 *         )
 *     }
 *
 *     Card(
 *         modifier = Modifier
 *             .size(cardStyle.width, cardStyle.height)
 *             .shadow(cardStyle.elevation, shape = RoundedCornerShape(cardStyle.cornerRadius))
 *             .border(cardStyle.borderWidth, cardStyle.borderColor, RoundedCornerShape(cardStyle.cornerRadius)),
 *         colors = CardDefaults.cardColors(containerColor = cardStyle.backgroundColor)
 *     ) {
 *         Box(modifier = Modifier.padding(cardStyle.padding)) {
 *             Text(cardStyle.title)
 *         }
 *     }
 * }
 * ```
 *
 * @param A Type of the first field value
 * @param B Type of the second field value
 * @param C Type of the third field value
 * @param D Type of the fourth field value
 * @param E Type of the fifth field value
 * @param F Type of the sixth field value
 * @param G Type of the seventh field value
 * @param H Type of the eighth field value
 * @param I Type of the ninth field value
 * @param Value The composite value type created by combining all nine field values
 * @param label Display label for the combined field
 * @param field1 First sub-field
 * @param field2 Second sub-field
 * @param field3 Third sub-field
 * @param field4 Fourth sub-field
 * @param field5 Fifth sub-field
 * @param field6 Sixth sub-field
 * @param field7 Seventh sub-field
 * @param field8 Eighth sub-field
 * @param field9 Ninth sub-field
 * @param combine Function to combine nine values into the composite value
 * @param split Function to split the composite value back into nine values
 * @return A new [CombinedField9] instance that manages the nine sub-fields as a single composite value
 */
public fun <A, B, C, D, E, F, G, H, I, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    field7: MutablePreviewLabField<G>,
    field8: MutablePreviewLabField<H>,
    field9: MutablePreviewLabField<I>,
    combine: (A, B, C, D, E, F, G, H, I) -> Value,
    split: (Value) -> Splited9<A, B, C, D, E, F, G, H, I>,
): CombinedField9<A, B, C, D, E, F, G, H, I, Value> = CombinedField9(
    label = label,
    field1 = field1,
    field2 = field2,
    field3 = field3,
    field4 = field4,
    field5 = field5,
    field6 = field6,
    field7 = field7,
    field8 = field8,
    field9 = field9,
    combine = combine,
    split = split,
)

// CombinedField10

/**
 * Data class that holds ten values split from a combined field.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param F Type of the sixth value
 * @param G Type of the seventh value
 * @param H Type of the eighth value
 * @param I Type of the ninth value
 * @param J Type of the tenth value
 * @param first The first split value
 * @param second The second split value
 * @param third The third split value
 * @param fourth The fourth split value
 * @param fifth The fifth split value
 * @param sixth The sixth split value
 * @param seventh The seventh split value
 * @param eighth The eighth split value
 * @param ninth The ninth split value
 * @param tenth The tenth split value
 */
public data class Splited10<A, B, C, D, E, F, G, H, I, J>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val fourth: D,
    public val fifth: E,
    public val sixth: F,
    public val seventh: G,
    public val eighth: H,
    public val ninth: I,
    public val tenth: J,
)

/**
 * Creates a [Splited10] instance containing ten values.
 *
 * @param A Type of the first value
 * @param B Type of the second value
 * @param C Type of the third value
 * @param D Type of the fourth value
 * @param E Type of the fifth value
 * @param F Type of the sixth value
 * @param G Type of the seventh value
 * @param H Type of the eighth value
 * @param I Type of the ninth value
 * @param J Type of the tenth value
 * @param first The first value
 * @param second The second value
 * @param third The third value
 * @param fourth The fourth value
 * @param fifth The fifth value
 * @param sixth The sixth value
 * @param seventh The seventh value
 * @param eighth The eighth value
 * @param ninth The ninth value
 * @param tenth The tenth value
 * @return A new [Splited10] instance
 */
public fun <A, B, C, D, E, F, G, H, I, J> splitedOf(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
    sixth: F,
    seventh: G,
    eighth: H,
    ninth: I,
    tenth: J,
): Splited10<A, B, C, D, E, F, G, H, I, J> =
    Splited10(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth)

public open class CombinedField10<A, B, C, D, E, F, G, H, I, J, Value>(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    field7: MutablePreviewLabField<G>,
    field8: MutablePreviewLabField<H>,
    field9: MutablePreviewLabField<I>,
    field10: MutablePreviewLabField<J>,
    private val combine: (A, B, C, D, E, F, G, H, I, J) -> Value,
    private val split: (Value) -> Splited10<A, B, C, D, E, F, G, H, I, J>,
) : CombinedField<Any?, Value>(
    label = label,
    fields = listOf(
        field1, field2, field3, field4, field5,
        field6, field7, field8, field9, field10,
    ),
    combine = {
        @Suppress("UNCHECKED_CAST")
        combine(
            it[0] as A, it[1] as B, it[2] as C, it[3] as D, it[4] as E,
            it[5] as F, it[6] as G, it[7] as H, it[8] as I, it[9] as J,
        )
    },
    split = { v ->
        val s = split(v)
        listOf(
            s.first, s.second, s.third, s.fourth, s.fifth,
            s.sixth, s.seventh, s.eighth, s.ninth, s.tenth,
        )
    },
)

/**
 * Creates a combined field from ten sub-fields.
 *
 * This function allows combining ten independent field values into a single composite value,
 * providing maximum flexibility for the most complex configurations or comprehensive data models.
 *
 * # Usage
 *
 * ```kotlin
 * // Complete UI theme configuration
 * data class ThemeConfig(
 *     val primaryColor: Color,
 *     val secondaryColor: Color,
 *     val backgroundColor: Color,
 *     val textColor: Color,
 *     val fontSize: TextUnit,
 *     val fontWeight: FontWeight,
 *     val cornerRadius: Dp,
 *     val spacing: Dp,
 *     val elevation: Dp,
 *     val darkMode: Boolean
 * )
 *
 * @Preview
 * @Composable
 * fun ThemeConfigFieldPreview() = PreviewLab {
 *     val theme: ThemeConfig = fieldValue {
 *         combined(
 *             label = "Theme Config",
 *             field1 = ColorField("Primary", Color(0xFF6200EE)),
 *             field2 = ColorField("Secondary", Color(0xFF03DAC6)),
 *             field3 = ColorField("Background", Color.White),
 *             field4 = ColorField("Text", Color.Black),
 *             field5 = TextUnitField("Font Size", 14.sp),
 *             field6 = EnumField("Font Weight", FontWeight.Normal),
 *             field7 = DpField("Corner Radius", 8.dp),
 *             field8 = DpField("Spacing", 16.dp),
 *             field9 = DpField("Elevation", 4.dp),
 *             field10 = BooleanField("Dark Mode", false),
 *             combine = { primary, secondary, bg, text, size, weight, radius, spacing, elev, dark ->
 *                 ThemeConfig(primary, secondary, bg, text, size, weight, radius, spacing, elev, dark)
 *             },
 *             split = { splitedOf(it.primaryColor, it.secondaryColor, it.backgroundColor, it.textColor, it.fontSize, it.fontWeight, it.cornerRadius, it.spacing, it.elevation, it.darkMode) }
 *         )
 *     }
 *
 *     Surface(
 *         color = theme.backgroundColor,
 *         modifier = Modifier.padding(theme.spacing)
 *     ) {
 *         Column(spacing = theme.spacing) {
 *             Text("Primary", color = theme.primaryColor, fontSize = theme.fontSize, fontWeight = theme.fontWeight)
 *             Text("Secondary", color = theme.secondaryColor, fontSize = theme.fontSize, fontWeight = theme.fontWeight)
 *             Text("Text", color = theme.textColor, fontSize = theme.fontSize, fontWeight = theme.fontWeight)
 *         }
 *     }
 * }
 * ```
 *
 * @param A Type of the first field value
 * @param B Type of the second field value
 * @param C Type of the third field value
 * @param D Type of the fourth field value
 * @param E Type of the fifth field value
 * @param F Type of the sixth field value
 * @param G Type of the seventh field value
 * @param H Type of the eighth field value
 * @param I Type of the ninth field value
 * @param J Type of the tenth field value
 * @param Value The composite value type created by combining all ten field values
 * @param label Display label for the combined field
 * @param field1 First sub-field
 * @param field2 Second sub-field
 * @param field3 Third sub-field
 * @param field4 Fourth sub-field
 * @param field5 Fifth sub-field
 * @param field6 Sixth sub-field
 * @param field7 Seventh sub-field
 * @param field8 Eighth sub-field
 * @param field9 Ninth sub-field
 * @param field10 Tenth sub-field
 * @param combine Function to combine ten values into the composite value
 * @param split Function to split the composite value back into ten values
 * @return A new [CombinedField10] instance that manages the ten sub-fields as a single composite value
 */
public fun <A, B, C, D, E, F, G, H, I, J, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    field7: MutablePreviewLabField<G>,
    field8: MutablePreviewLabField<H>,
    field9: MutablePreviewLabField<I>,
    field10: MutablePreviewLabField<J>,
    combine: (A, B, C, D, E, F, G, H, I, J) -> Value,
    split: (Value) -> Splited10<A, B, C, D, E, F, G, H, I, J>,
): CombinedField10<A, B, C, D, E, F, G, H, I, J, Value> = CombinedField10(
    label = label,
    field1 = field1,
    field2 = field2,
    field3 = field3,
    field4 = field4,
    field5 = field5,
    field6 = field6,
    field7 = field7,
    field8 = field8,
    field9 = field9,
    field10 = field10,
    combine = combine,
    split = split,
)
