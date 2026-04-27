package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.name.FqName

/**
 * `IrAnnotationContainer` から特定の annotation を取得する helper。
 *
 * 標準ライブラリの `IrUtilsKt.getAnnotation(...)` は Kotlin 2.4 で戻り値型が
 * `IrConstructorCall?` から `IrAnnotation?` に変わったため `NoSuchMethodError` を起こす。
 * 本実装は `IrAnnotationContainer.annotations` (version 不変な list) を直接走査することで、
 * 2.3 / 2.4 双方で動作する。
 *
 * 注: 戻り値型は `IrConstructorCall?`。Kotlin 2.4 の `IrAnnotation` は `IrConstructorCall` の
 * サブタイプなので問題なくダウンキャストできる。
 */
public fun IrAnnotationContainer.getAnnotationCompat(fqName: FqName): IrConstructorCall? =
    annotations.firstOrNull { call -> call.type.classFqName == fqName }

/** [getAnnotationCompat] の boolean 版。 */
public fun IrAnnotationContainer.hasAnnotationCompat(fqName: FqName): Boolean =
    annotations.any { call -> call.type.classFqName == fqName }
