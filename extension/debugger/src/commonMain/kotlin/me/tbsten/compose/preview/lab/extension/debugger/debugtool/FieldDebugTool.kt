package me.tbsten.compose.preview.lab.extension.debugger.debugtool

import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.extension.debugger.DebugTool

class FieldDebugTool<Value>(val field: PreviewLabField<Value>) : DebugTool {
    override val title: String by field::label
    val value by field::value

    @Composable
    override fun Content() {
        field.View()
    }
}

fun <Value, Field : PreviewLabField<Value>> Field.toDebugTool() = FieldDebugTool<Value>(
    field = this,
)

fun <DebugBehavior, Result> (suspend () -> Result).debuggable(
    debugger: FieldDebugTool<DebugBehavior>,
    behavior: suspend DebuggableBehavior<Result>.(debugBehavior: DebugBehavior) -> Result,
): suspend () -> Result = {
    val base = this@debuggable
    val runDefault = suspend { base.invoke() }

    behavior(
        DebuggableBehavior(runDefault = runDefault),
        debugger.value,
    )
}

class DebuggableBehavior<Result>(val runDefault: suspend () -> Result)
