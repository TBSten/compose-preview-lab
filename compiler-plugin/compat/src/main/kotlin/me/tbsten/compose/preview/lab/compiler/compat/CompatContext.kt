package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType

/**
 * Kotlin compiler API の version-specific 差異を吸収する SPI。
 *
 * 実装は [Factory] として `META-INF/services/.../CompatContext$Factory` で登録され、
 * [CompatContextLoader] が runtime に Kotlin バージョンを判定して最適な実装を選択する。
 *
 * 新しい Kotlin バージョンで API drift が発生したら、ここに抽象メソッドを追加し、
 * 各 compat module ([Factory.minVersion]) で実装する。
 */
public interface CompatContext {
    /**
     * FIR declaration が関数かを判定する。
     *
     * - Kotlin 2.2 以前: `FirSimpleFunction`
     * - Kotlin 2.3 以降: `FirFunction` (FirSimpleFunction が統合された)
     */
    public fun isFirFunction(declaration: FirDeclaration): Boolean

    /**
     * 指定された constructor symbol からアノテーションを生成して関数に追加する。
     *
     * - Kotlin 2.3 以前: `IrConstructorCallImpl` を直接 annotations に追加
     * - Kotlin 2.4 以降: `IrAnnotationImpl` を使用 (`IrSimpleFunction.annotations` の要素型変更)
     */
    public fun addConstructorCallAnnotation(function: IrSimpleFunction, type: IrType, constructorSymbol: IrConstructorSymbol,)

    /**
     * compat 実装の factory。各 k* module は自身の [minVersion] を持つ実装を
     * `META-INF/services/me.tbsten.compose.preview.lab.compiler.compat.CompatContext${'$'}Factory` に登録する。
     */
    public interface Factory {
        /** この実装が対応する最小 Kotlin バージョン (例: "2.3.0", "2.4.0-Beta2")。 */
        public val minVersion: String

        public fun create(): CompatContext
    }

    public companion object {
        /**
         * 現在の Kotlin compiler バージョンに最適な [CompatContext] 実装をロードする。
         *
         * @param knownVersion テストや明示指定用。null なら META-INF/compiler.version から自動検出。
         */
        public fun load(knownVersion: KotlinToolingVersion? = null): CompatContext = CompatContextLoader.load(knownVersion)
    }
}
