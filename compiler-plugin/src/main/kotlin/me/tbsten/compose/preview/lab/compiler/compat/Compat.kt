package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType

/**
 * Version-specific Kotlin compiler API へのアクセスポイント。
 *
 * 実際の差分実装は [CompatContext] を実装した compat module
 * (`compiler-plugin-compat-k230`, `compiler-plugin-compat-k240_beta2` 等) に閉じ込められており、
 * 実行時に [ServiceLoader] 経由で現在の Kotlin compiler バージョンに合うものが選ばれる。
 *
 * 呼び出し側のコード変更を最小化するため、これまで `kotlin-2.3/` 等の sourceSet で定義されていた
 * extension function と同じシグネチャを維持している。
 */
private val compatContext: CompatContext by lazy { CompatContext.load() }

/** FIR declaration が関数かを判定する。バージョン別実装は [CompatContext.isFirFunction] を参照。 */
internal fun FirDeclaration.isFirFunction(): Boolean = compatContext.isFirFunction(this)

/**
 * 指定された constructor symbol からアノテーションを生成して関数に追加する。
 * バージョン別実装は [CompatContext.addConstructorCallAnnotation] を参照。
 */
internal fun IrSimpleFunction.addConstructorCallAnnotation(type: IrType, constructorSymbol: IrConstructorSymbol,) =
    compatContext.addConstructorCallAnnotation(this, type, constructorSymbol)
