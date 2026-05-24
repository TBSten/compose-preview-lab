@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.ir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.AUTO_EVENT_FQN
import me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.AUTO_FIELD_FQN
import me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.AutoLabelParameterName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * IR transformer that injects the surrounding parameter name as the `label` argument of
 * unlabelled `autoField()` / `autoEvent()` calls.
 *
 * The transform runs once per IR call expression and only modifies the `label` argument
 * of the inner `autoField` / `autoEvent` call when (a) it appears as a value argument of
 * another call, and (b) the user did not pass `label` explicitly.
 *
 * **Before**:
 * ```kotlin
 * UserCard(
 *     name = autoField(),
 *     age = autoField(),
 *     onClick = autoEvent(),
 * )
 * ```
 *
 * **After** (semantically equivalent):
 * ```kotlin
 * UserCard(
 *     name = autoField(label = "name"),
 *     age = autoField(label = "age"),
 *     onClick = autoEvent(label = "onClick"),
 * )
 * ```
 *
 * Explicit-label call sites are left untouched, so `autoField(label = "Custom")` keeps
 * its user-supplied value. Call sites with no surrounding parameter (e.g. a bare
 * `val x: String = autoField()` at file top level) are also left untouched — the runtime
 * default `"auto"` continues to apply.
 */
internal class InjectAutoLabelIrTransformer(
    private val pluginContext: IrPluginContext,
    private val compatContext: CompatContext,
) : IrElementTransformerVoid() {

    /**
     * Walks every [IrCall] and rewrites the `label` argument of any `autoField` / `autoEvent`
     * value-argument call whose label was left at the declared default.
     *
     * **Before** (a call site like `UserCard(name = autoField(), onClick = autoEvent())`):
     * ```kotlin
     * IrCall(UserCard)
     *   arguments[0] = IrCall(autoField, args = [/* extension receiver */, label = null])
     *   arguments[1] = IrCall(autoEvent, args = [/* extension receiver */, label = null])
     * ```
     *
     * **After**:
     * ```kotlin
     * IrCall(UserCard)
     *   arguments[0] = IrCall(autoField, args = [..., label = IrConst("name")])
     *   arguments[1] = IrCall(autoEvent, args = [..., label = IrConst("onClick")])
     * ```
     */
    override fun visitCall(expression: IrCall): IrExpression {
        injectLabelsForChildren(expression)
        return super.visitCall(expression)
    }

    /**
     * Scans [parentCall]'s positional arguments and, for any slot whose value is an unlabeled
     * `autoField` / `autoEvent` call, rewrites that inner call's `label` argument to embed
     * the parent's parameter name.
     *
     * For a call `UserCard(name = autoField())`:
     * - `parentCall.arguments[i]` is the inner `autoField()` `IrCall`.
     * - `parentCall.symbol.owner.parameters[i]` is the regular parameter `name: String`.
     * - The inner call's `label` parameter starts as `null` (default unfilled at this IR
     *   phase) and is rewritten to `IrConst("name")`.
     */
    private fun injectLabelsForChildren(parentCall: IrFunctionAccessExpression) {
        val parentParameters = parentCall.symbol.owner.parameters
        for ((index, arg) in parentCall.arguments.withIndex()) {
            if (arg !is IrCall) continue
            if (!arg.isAutoFieldOrEventCall()) continue
            if (index >= parentParameters.size) continue
            val parentParam = parentParameters[index]
            if (parentParam.kind != IrParameterKind.Regular) continue
            injectLabelIfDefault(arg, parentParam.name.asString())
        }
    }

    /**
     * Mutates [autoCall]'s `label` argument to [labelValue] when it is still at the
     * declared default (i.e. `arguments[labelIndex] == null`).
     *
     * Returns silently when:
     * - the call does not declare a `label` parameter (defensive — the FQN check should
     *   guarantee this, but layout drift between the plugin and the runtime API would land
     *   here rather than throw)
     * - the user already supplied `label` explicitly (we never overwrite a user-supplied
     *   argument)
     */
    private fun injectLabelIfDefault(autoCall: IrCall, labelValue: String) {
        val labelIndex = autoCall.symbol.owner.findRegularParameterIndex(AutoLabelParameterName)
        if (labelIndex < 0) return
        if (autoCall.arguments.getOrNull(labelIndex) != null) return
        val builder = DeclarationIrBuilder(pluginContext, autoCall.symbol)
        autoCall.arguments[labelIndex] = compatContext.irString(builder, labelValue)
    }

    private fun IrCall.isAutoFieldOrEventCall(): Boolean {
        val fqn = symbol.owner.kotlinFqName
        return fqn == AUTO_FIELD_FQN || fqn == AUTO_EVENT_FQN
    }
}

/**
 * Returns the [parameters] index of the regular value parameter named [name], or `-1`
 * when no such parameter exists.
 *
 * **Sample call → result**:
 * ```kotlin
 * // fun PreviewLabScope.autoField(label: String = "auto"): T
 * autoFieldFun.findRegularParameterIndex("label") // → 1 (after the extension receiver)
 * autoFieldFun.findRegularParameterIndex("absent") // → -1
 * ```
 *
 * Receiver / context parameters are skipped via [IrParameterKind.Regular]; the returned
 * index is into the unified `parameters` list and can be used directly with
 * `IrCall.arguments[index]`.
 */
private fun IrSimpleFunction.findRegularParameterIndex(name: String): Int =
    parameters.indexOfFirst { it.kind == IrParameterKind.Regular && it.name.asString() == name }
