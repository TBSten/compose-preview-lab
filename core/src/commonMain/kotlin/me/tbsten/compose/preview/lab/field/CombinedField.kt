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
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.Text

open class CombinedField<Base, Value>(
    label: String,
    private val fields: List<MutablePreviewLabField<out Base>>,
    private val combine: (List<Base>) -> Value,
    private val split: (Value) -> List<Base>,
) : MutablePreviewLabField<Value>(
    label = label,
    initialValue = combine(fields.map { it.value }),
) {
    private val _value by derivedStateOf {
        combine(fields.map { it.value })
    }

    override var value: Value
        get() = _value
        set(value) {
            split(value).forEachIndexed { index, value ->
                @Suppress("CAST_NEVER_SUCCEEDS")
                fields[index].value = value as Nothing
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

// CombinedField2
data class Splited2<A, B>(val first: A, val second: B)

fun <A, B> splitedOf(first: A, second: B) = Splited2(first, second)

open class CombinedField2<A, B, Value>(
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

fun <A, B, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    combine: (A, B) -> Value,
    split: (Value) -> Splited2<A, B>,
) = CombinedField2(
    label = label,
    field1 = field1,
    field2 = field2,
    combine = combine,
    split = split,
)

// CombinedField3
data class Splited3<A, B, C>(val first: A, val second: B, val third: C)

fun <A, B, C> splitedOf(first: A, second: B, third: C) = Splited3(first, second, third)

open class CombinedField3<A, B, C, Value>(
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

fun <A, B, C, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    combine: (A, B, C) -> Value,
    split: (Value) -> Splited3<A, B, C>,
) = CombinedField3(
    label = label,
    field1 = field1,
    field2 = field2,
    field3 = field3,
    combine = combine,
    split = split,
)

// CombinedField4
data class Splited4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

fun <A, B, C, D> splitedOf(first: A, second: B, third: C, fourth: D) = Splited4(first, second, third, fourth)

open class CombinedField4<A, B, C, D, Value>(
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

fun <A, B, C, D, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    combine: (A, B, C, D) -> Value,
    split: (Value) -> Splited4<A, B, C, D>,
) = CombinedField4(
    label = label,
    field1 = field1,
    field2 = field2,
    field3 = field3,
    field4 = field4,
    combine = combine,
    split = split,
)

// CombinedField5
data class Splited5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

fun <A, B, C, D, E> splitedOf(first: A, second: B, third: C, fourth: D, fifth: E) =
    Splited5(first, second, third, fourth, fifth)

open class CombinedField5<A, B, C, D, E, Value>(
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

fun <A, B, C, D, E, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    combine: (A, B, C, D, E) -> Value,
    split: (Value) -> Splited5<A, B, C, D, E>,
) = CombinedField5(
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
data class Splited6<A, B, C, D, E, F>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E, val sixth: F)

fun <A, B, C, D, E, F> splitedOf(first: A, second: B, third: C, fourth: D, fifth: E, sixth: F) =
    Splited6(first, second, third, fourth, fifth, sixth)

open class CombinedField6<A, B, C, D, E, F, Value>(
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

fun <A, B, C, D, E, F, Value> combined(
    label: String,
    field1: MutablePreviewLabField<A>,
    field2: MutablePreviewLabField<B>,
    field3: MutablePreviewLabField<C>,
    field4: MutablePreviewLabField<D>,
    field5: MutablePreviewLabField<E>,
    field6: MutablePreviewLabField<F>,
    combine: (A, B, C, D, E, F) -> Value,
    split: (Value) -> Splited6<A, B, C, D, E, F>,
) = CombinedField6(
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
data class Splited7<A, B, C, D, E, F, G>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
)

fun <A, B, C, D, E, F, G> splitedOf(first: A, second: B, third: C, fourth: D, fifth: E, sixth: F, seventh: G) =
    Splited7(first, second, third, fourth, fifth, sixth, seventh)

open class CombinedField7<A, B, C, D, E, F, G, Value>(
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

fun <A, B, C, D, E, F, G, Value> combined(
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
) = CombinedField7(
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
data class Splited8<A, B, C, D, E, F, G, H>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
)

fun <A, B, C, D, E, F, G, H> splitedOf(first: A, second: B, third: C, fourth: D, fifth: E, sixth: F, seventh: G, eighth: H) =
    Splited8(first, second, third, fourth, fifth, sixth, seventh, eighth)

open class CombinedField8<A, B, C, D, E, F, G, H, Value>(
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

fun <A, B, C, D, E, F, G, H, Value> combined(
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
) = CombinedField8(
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
data class Splited9<A, B, C, D, E, F, G, H, I>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
    val ninth: I,
)

fun <A, B, C, D, E, F, G, H, I> splitedOf(
    first: A,
    second: B,
    third: C,
    fourth: D,
    fifth: E,
    sixth: F,
    seventh: G,
    eighth: H,
    ninth: I,
) = Splited9(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth)

open class CombinedField9<A, B, C, D, E, F, G, H, I, Value>(
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

fun <A, B, C, D, E, F, G, H, I, Value> combined(
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
) = CombinedField9(
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
data class Splited10<A, B, C, D, E, F, G, H, I, J>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
    val ninth: I,
    val tenth: J,
)

fun <A, B, C, D, E, F, G, H, I, J> splitedOf(
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
) = Splited10(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth)

open class CombinedField10<A, B, C, D, E, F, G, H, I, J, Value>(
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

fun <A, B, C, D, E, F, G, H, I, J, Value> combined(
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
) = CombinedField10(
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
