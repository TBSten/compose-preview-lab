package me.tbsten.compose.preview.lab.extension.debugger.debugtool

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import me.tbsten.compose.preview.lab.MutablePreviewLabField
import me.tbsten.compose.preview.lab.field.DoubleField
import me.tbsten.compose.preview.lab.field.FixedField
import me.tbsten.compose.preview.lab.field.NumberField
import me.tbsten.compose.preview.lab.field.PolymorphicField
import me.tbsten.compose.preview.lab.field.StringField
import me.tbsten.compose.preview.lab.field.combined
import me.tbsten.compose.preview.lab.field.nullable
import me.tbsten.compose.preview.lab.field.splitedOf
import me.tbsten.compose.preview.lab.field.transform
import me.tbsten.compose.preview.lab.field.withHint
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText

/**
 * Creates a [DebugTool] that allows modifying the behavior of a suspend function from the debug menu.
 *
 * This can be thought of as an advanced alternative to Fakes in unit testing.
 * For example, you can test the behavior of ViewModels by modifying the behavior
 * of UseCase or Repository methods from the debug menu.
 *
 * # Example
 *
 * ```kotlin
 * object AppDebugMenu : DebugMenu() {
 *     val getItemListUseCaseBehavior = tool {
 *         basicFunctionDebugBehavior(
 *             label = "getItemListUseCaseBehavior",
 *             returnValueField = EnumField<GetItemListUseCaseDebugBehavior>(
 *                 "Result",
 *                 GetItemListUseCaseDebugBehavior.Normal,
 *             ),
 *         )
 *     }
 * }
 *
 * enum class GetItemListUseCaseDebugBehavior {
 *     EmptyFake,
 *     ManyListFake,
 * }
 * ```
 *
 * Use [debuggableBasic] to wrap your function and apply the debug behavior:
 *
 * ```kotlin
 * class DebugGetItemListUseCase(
 *     private val default: GetItemListUseCase,
 * ) : GetItemListUseCase {
 *     override fun execute(): List<Item> = suspend { default.execute() }
 *         .debuggableBasic(AppDebugMenu.getItemListUseCaseBehavior) { behavior ->
 *             when(behavior) {
 *                 EmptyFake -> emptyList()
 *                 ManyListFake -> Item.manyListFake()
 *             }
 *         }.invoke()
 * }
 * ```
 *
 * @param label The label displayed in the debug menu
 * @param returnValueField Field for configuring the return value type
 * @param errorMessageField Field for configuring error message when simulating errors
 * @param cancelMessageField Field for configuring cancel message when simulating cancellation
 *
 * @see debuggableBasic
 * @see DebugTool
 */
fun <Result> basicFunctionDebugBehavior(
    label: String,
    returnValueField: MutablePreviewLabField<Result>?,
    errorMessageField: MutablePreviewLabField<String?> =
        BasicFunctionDebugBehaviorResultFields.DefaultErrorFieldMessageField(),
    cancelMessageField: MutablePreviewLabField<String?> =
        BasicFunctionDebugBehaviorResultFields.DefaultCancelFieldMessageField(),
) = combined(
    label = label,
    field1 = DoubleField(
        "delay",
        0.0,
        NumberField.InputType.TextField(suffix = { PreviewLabText("sec") }),
    ).transform({ it.seconds }, { it.inWholeNanoseconds / 1_000_000_000.0 })
        .withHint(
            "0.sec" to 0.seconds,
            "1.sec" to 1.seconds,
            "3.sec" to 3.seconds,
        ),
    field2 = BasicFunctionDebugBehaviorResultField(
        "result",
        returnValueField = returnValueField,
        errorMessageField = errorMessageField,
        cancelMessageField = cancelMessageField,
    ),
    combine = { delay, result ->
        BasicFunctionDebugBehavior(delay = delay, result = result)
    },
    split = { splitedOf(it.delay, it.result) },
).toDebugTool()

fun <Result> (suspend () -> Result).debuggable(
    debugger: FieldDebugTool<BasicFunctionDebugBehavior<Result>>,
    behavior: suspend DebuggableBehavior<Result>.(debugBehavior: BasicFunctionDebugBehavior<Result>) -> Result,
): suspend () -> Result = debuggable<BasicFunctionDebugBehavior<Result>, Result>(
    debugger = debugger,
    behavior = behavior,
)

fun basicFunctionDebugBehavior(
    label: String,
    errorMessageField: MutablePreviewLabField<String?> =
        BasicFunctionDebugBehaviorResultFields.DefaultErrorFieldMessageField(),
    cancelMessageField: MutablePreviewLabField<String?> =
        BasicFunctionDebugBehaviorResultFields.DefaultCancelFieldMessageField(),
) = basicFunctionDebugBehavior<Unit>(
    label = label,
    returnValueField = null,
    errorMessageField = errorMessageField,
    cancelMessageField = cancelMessageField,
)

data class BasicFunctionDebugBehavior<R>(val delay: Duration, val result: Result<R>) {
    sealed interface Result<R> {
        class Default<R> : Result<R>
        data class Return<R>(val resultType: R) : Result<R>
        data class Error<R>(val throwable: () -> Throwable) : Result<R>
        data class Cancel<R>(
            val exception: () -> CancellationException = {
                CancellationException(message = null, cause = null)
            },
        ) : Result<R>
    }
}

@Suppress("FunctionName")
fun <R> BasicFunctionDebugBehaviorResultField(
    label: String,
    returnValueField: MutablePreviewLabField<R>?,
    errorMessageField: MutablePreviewLabField<String?> =
        BasicFunctionDebugBehaviorResultFields.DefaultErrorFieldMessageField(),
    cancelMessageField: MutablePreviewLabField<String?> =
        BasicFunctionDebugBehaviorResultFields.DefaultCancelFieldMessageField(),
) = with(BasicFunctionDebugBehaviorResultFields) {
    PolymorphicField<BasicFunctionDebugBehavior.Result<R>>(
        label = label,
        initialValue = BasicFunctionDebugBehavior.Result.Default(),
        fields = buildList {
            add(DefaultField())
            returnValueField?.let { add(ReturnField(it)) }
            add(ErrorField(errorMessageField))
            add(CancelField(cancelMessageField))
        },
    )
}

object BasicFunctionDebugBehaviorResultFields {
    @Suppress("FunctionName")
    fun <R> DefaultField() = FixedField(
        "Default",
        BasicFunctionDebugBehavior.Result.Default<R>(),
    )

    @Suppress("FunctionName")
    fun <R> ReturnField(returnValueField: MutablePreviewLabField<R>) = returnValueField.transform(
        transform = { BasicFunctionDebugBehavior.Result.Return(it) },
        reverse = { it.resultType },
    )

    @Suppress("FunctionName")
    fun DefaultErrorFieldMessageField(initialMessage: String? = "Error from debug menu") =
        StringField("Error", initialMessage ?: "")
            .nullable(initialValue = initialMessage)

    // TODO Error polymorphic field
    @Suppress("FunctionName")
    fun <R> ErrorField(messageField: MutablePreviewLabField<String?> = DefaultErrorFieldMessageField()) =
        messageField.transform(
            transform = { BasicFunctionDebugBehavior.Result.Error<R>({ IllegalStateException(it) }) },
            reverse = { it.throwable().message },
        )

    @Suppress("FunctionName")
    fun DefaultCancelFieldMessageField(initialMessage: String? = "Cancel") = StringField("Cancel", initialMessage ?: "")
        .nullable(initialValue = initialMessage)

    // TODO Error polymorphic field
    @Suppress("FunctionName")
    fun <R> CancelField(messageField: MutablePreviewLabField<String?> = DefaultCancelFieldMessageField()) =
        messageField.transform(
            transform = { BasicFunctionDebugBehavior.Result.Cancel<R>({ CancellationException(it) }) },
            reverse = { it.exception().message },
        )
}

/**
 * Wraps this suspend function to make it controllable from the debug menu.
 *
 * When the debug menu is configured, this function will apply the specified behavior
 * (delay, return value override, error simulation, or cancellation) instead of
 * executing the original function.
 *
 * @param debugger The debug tool created by [basicFunctionDebugBehavior]
 * @param onResult Transform function to convert the debug behavior result to the actual return type
 *
 * @see basicFunctionDebugBehavior
 */
fun <ResultType, Result> (suspend () -> Result).debuggableBasic(
    debugger: FieldDebugTool<out BasicFunctionDebugBehavior<ResultType>>,
    onResult: (ResultType) -> Result,
): suspend () -> Result = debuggable(
    debugger = debugger,
    behavior = { debugBehavior ->
        delay(debugBehavior.delay)

        when (val result = debugBehavior.result) {
            is BasicFunctionDebugBehavior.Result.Default<ResultType> -> runDefault()
            is BasicFunctionDebugBehavior.Result.Return<ResultType> -> onResult(result.resultType)
            is BasicFunctionDebugBehavior.Result.Error<ResultType> -> throw result.throwable()
            is BasicFunctionDebugBehavior.Result.Cancel<ResultType> -> throw result.exception()
        }
    },
)

fun (suspend () -> Unit).debuggableBasic(debugger: FieldDebugTool<out BasicFunctionDebugBehavior<Unit>>): suspend () -> Unit =
    debuggableBasic(
        debugger = debugger,
        onResult = { Unit },
    )
