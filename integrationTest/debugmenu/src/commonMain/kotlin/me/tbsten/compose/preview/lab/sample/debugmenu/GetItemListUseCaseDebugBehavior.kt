package me.tbsten.compose.preview.lab.sample.debugmenu

import androidx.compose.material3.Text
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import me.tbsten.compose.preview.lab.extension.debugger.debugtool.toDebugTool
import me.tbsten.compose.preview.lab.field.DoubleField
import me.tbsten.compose.preview.lab.field.EnumField
import me.tbsten.compose.preview.lab.field.NumberField
import me.tbsten.compose.preview.lab.field.combined
import me.tbsten.compose.preview.lab.field.splitedOf
import me.tbsten.compose.preview.lab.field.transform

data class GetItemListUseCaseDebugBehavior(val delay: Duration, val result: Result) {
    enum class Result {
        Default,
        ReturnFakeNormal,
        ReturnFakeEmpty,
        Error,
        Cancel,
    }
}

fun getItemListUseCaseDebugBehavior() = combined(
    label = "GetItemListUseCase",
    field1 = DoubleField(
        "delay",
        0.0,
        NumberField.InputType.TextField(suffix = { Text("sec") }),
    ).transform({ it.seconds }, { it.inWholeNanoseconds / 1_000_000_000.0 }),
    field2 = EnumField<GetItemListUseCaseDebugBehavior.Result>(
        "result",
        GetItemListUseCaseDebugBehavior.Result.Default,
    ),
    combine = { delay, result ->
        GetItemListUseCaseDebugBehavior(delay = delay, result = result)
    },
    split = { splitedOf(it.delay, it.result) },
).toDebugTool()
