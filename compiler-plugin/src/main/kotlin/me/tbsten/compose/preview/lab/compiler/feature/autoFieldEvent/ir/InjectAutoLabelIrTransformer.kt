@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.ir

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.error.RuntimeFunctionNotFoundError
import me.tbsten.compose.preview.lab.compiler.error.throwAsException
import me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.AUTO_ENUM_FIELD_CALLABLE_ID
import me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.AUTO_EVENT_FQN
import me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.AUTO_FIELD_FQN
import me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.AUTO_NULLABLE_ENUM_FIELD_CALLABLE_ID
import me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.AutoLabelParameterName
import me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent.SUPPORTED_AUTO_FIELD_TYPE_FQNS
import me.tbsten.compose.preview.lab.compiler.utils.ir.compilerMessageLocation
import me.tbsten.compose.preview.lab.compiler.warning.UnsupportedAutoFieldTypeWarning
import me.tbsten.compose.preview.lab.compiler.warning.report
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * IR transformer that injects the surrounding parameter name as the `label` argument of
 * unlabelled `autoField()` / `autoEvent()` calls, and specialises `autoField<T>()` by its type
 * argument.
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
 *     plan = autoField(),          // plan: Plan (enum class)
 *     onClick = autoEvent(),
 * )
 * ```
 *
 * **After** (semantically equivalent):
 * ```kotlin
 * UserCard(
 *     name = autoField(label = "name"),
 *     age = autoField(label = "age"),
 *     plan = autoEnumField<Plan>(label = "plan"),
 *     onClick = autoEvent(label = "onClick"),
 * )
 * ```
 *
 * Explicit-label call sites are left untouched, so `autoField(label = "Custom")` keeps
 * its user-supplied value. Call sites with no surrounding parameter (e.g. a bare
 * `val x: String = autoField()` at file top level) are also left untouched — the runtime
 * default `"auto"` continues to apply (the enum rewrite still happens there).
 *
 * `autoField<T>()` with a concrete `T` the runtime has no default Field for
 * (anything but `String` / `Int` / `Long` / `Byte` / `Float` / `Double` / `Boolean`, their
 * nullable variants, or an enum class) compiles unchanged but reports
 * [UnsupportedAutoFieldTypeWarning] at the call site, since the call will throw at runtime.
 */
internal class InjectAutoLabelIrTransformer(
    private val pluginContext: IrPluginContext,
    private val compatContext: CompatContext,
    private val messageCollector: MessageCollector,
) : IrElementTransformerVoid() {

    /** The file whose declarations are currently being visited; used to locate warnings. */
    private var currentFile: IrFile? = null

    override fun visitFile(declaration: IrFile): IrFile {
        currentFile = declaration
        return super.visitFile(declaration)
    }

    /**
     * Walks every [IrCall], rewrites the `label` argument of any `autoField` / `autoEvent`
     * value-argument call whose label was left at the declared default, and then specialises
     * the call itself if it is an `autoField<T>()` (see [specialiseAutoFieldByType]).
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
        val visited = super.visitCall(expression)
        if (expression.isAutoFieldCall()) {
            return specialiseAutoFieldByType(expression) ?: visited
        }
        return visited
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

    /**
     * Looks at the type argument of an `autoField<T>()` call and either rewrites it to the enum
     * entry point or reports that the runtime has no default Field for `T`.
     *
     * **Before → After** (`T` is an enum class):
     * ```kotlin
     * plan = autoField<Plan>(label = "plan")
     * // → plan = autoEnumField<Plan>(label = "plan")
     * optionalPlan = autoField<Plan?>(label = "optionalPlan")
     * // → optionalPlan = autoNullableEnumField<Plan>(label = "optionalPlan")
     * ```
     *
     * **Before → unchanged + warning** (`T` is a class the runtime cannot build a Field for):
     * ```kotlin
     * user = autoField<User>(label = "user")
     * // warning: [ComposePreviewLab/IR,INVALID_USAGE] autoField<com.example.User>() has no default Field …
     * ```
     *
     * Returns `null` (= keep the original call) for supported primitives and for type parameters
     * (`autoField<T>()` inside a generic helper), where nothing can be decided at compile time.
     */
    private fun specialiseAutoFieldByType(autoCall: IrCall): IrExpression? {
        val typeArgument = autoCall.typeArguments.firstOrNull() as? IrSimpleType ?: return null
        val irClass = typeArgument.classOrNull?.owner ?: return null
        if (irClass.isEnumClass) {
            return rewriteToEnumField(autoCall, typeArgument)
        }
        val typeFqn = irClass.kotlinFqName.asString()
        if (typeFqn !in SUPPORTED_AUTO_FIELD_TYPE_FQNS) {
            messageCollector.report(
                UnsupportedAutoFieldTypeWarning(typeFqn),
                currentFile?.let { autoCall.compilerMessageLocation(it) },
            )
        }
        return null
    }

    /**
     * Builds the `autoEnumField<E>(label)` / `autoNullableEnumField<E>(label)` call that replaces
     * an `autoField<E>()` / `autoField<E?>()` call whose `E` is an enum class.
     *
     * **Sample call → Resulting IR**:
     * ```kotlin
     * // autoCall = IrCall(autoField, typeArguments = [Plan?], args = [scope, label = IrConst("plan")])
     * rewriteToEnumField(autoCall, Plan?)
     * // → IrCall(autoNullableEnumField, typeArguments = [Plan], args = [scope, label = IrConst("plan")])
     * //   with `type` kept as the original expression type (`Plan?`)
     * ```
     *
     * The extension receiver and the `label` argument are carried over by parameter kind / name,
     * so a `null` (= default) label stays `null`.
     */
    private fun rewriteToEnumField(autoCall: IrCall, enumType: IrSimpleType): IrExpression {
        val calleeId = if (enumType.isMarkedNullable()) AUTO_NULLABLE_ENUM_FIELD_CALLABLE_ID else AUTO_ENUM_FIELD_CALLABLE_ID
        val callee = pluginContext.referenceFunctions(calleeId).singleOrNull()
            ?: RuntimeFunctionNotFoundError(calleeId).throwAsException()

        val builder = DeclarationIrBuilder(pluginContext, autoCall.symbol, autoCall.startOffset, autoCall.endOffset)
        val enumFieldCall = compatContext.irCall(builder, callee)
        enumFieldCall.type = autoCall.type
        enumFieldCall.typeArguments[0] = enumType.makeNotNull()

        val source = autoCall.symbol.owner
        val target = callee.owner
        enumFieldCall.arguments[target.findParameterIndex(IrParameterKind.ExtensionReceiver)] =
            autoCall.arguments[source.findParameterIndex(IrParameterKind.ExtensionReceiver)]
        enumFieldCall.arguments[target.findRegularParameterIndex(AutoLabelParameterName)] =
            autoCall.arguments[source.findRegularParameterIndex(AutoLabelParameterName)]
        return enumFieldCall
    }

    private fun IrCall.isAutoFieldCall(): Boolean = symbol.owner.kotlinFqName == AUTO_FIELD_FQN

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

/**
 * Returns the [parameters] index of the first parameter of the given [kind], or `-1`.
 *
 * **Sample call → result**:
 * ```kotlin
 * // fun PreviewLabScope.autoField(label: String = "auto"): T
 * autoFieldFun.findParameterIndex(IrParameterKind.ExtensionReceiver) // → 0
 * ```
 */
private fun IrSimpleFunction.findParameterIndex(kind: IrParameterKind): Int = parameters.indexOfFirst { it.kind == kind }
