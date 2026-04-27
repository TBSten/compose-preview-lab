package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.name.FqName

/**
 * Helper that fetches a specific annotation from an `IrAnnotationContainer`.
 *
 * The standard library's `IrUtilsKt.getAnnotation(...)` had its return type changed
 * from `IrConstructorCall?` to `IrAnnotation?` in Kotlin 2.4, so calling it triggers
 * `NoSuchMethodError`. This implementation walks `IrAnnotationContainer.annotations`
 * (a list whose shape is unchanged across versions), so it works on both 2.3 and 2.4.
 *
 * Note: the return type stays `IrConstructorCall?`. Kotlin 2.4's `IrAnnotation` is a
 * subtype of `IrConstructorCall`, so the downcast is safe.
 */
public fun IrAnnotationContainer.getAnnotationCompat(fqName: FqName): IrConstructorCall? =
    annotations.firstOrNull { call -> call.type.classFqName == fqName }

/** Boolean variant of [getAnnotationCompat]. */
public fun IrAnnotationContainer.hasAnnotationCompat(fqName: FqName): Boolean =
    annotations.any { call -> call.type.classFqName == fqName }
