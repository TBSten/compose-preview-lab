package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin

/**
 * `IrDeclarationOrigin.<NAME>` を Kotlin patch バージョン非依存にアクセスする helper。
 *
 * Kotlin 2.3 系では `IrDeclarationOrigin` の宣言が patch ごとに変わる:
 * - 2.3.0 / 2.3.10: `companion object` で `LOCAL_FUNCTION_FOR_LAMBDA` を持つ → bytecode 上は `Companion.getLOCAL_FUNCTION_FOR_LAMBDA()`
 * - 2.3.20 以降: top-level object のメンバとして定義 → bytecode 上は static field GET
 *
 * 直接 `IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA` を書くと、コンパイル時の Kotlin compiler API
 * とは異なる runtime jar との間で `NoSuchMethodError` が起きる。
 * 本 helper は両形式を reflection で試して動作する方を返す。
 */
public object IrDeclarationOriginCompat {
    /** `LOCAL_FUNCTION_FOR_LAMBDA` を取得。2.3.0/10 (companion accessor) と 2.3.20+ (static field) 両方に対応。 */
    public val LOCAL_FUNCTION_FOR_LAMBDA: IrDeclarationOrigin by lazy {
        lookup("LOCAL_FUNCTION_FOR_LAMBDA")
    }

    /** `DELEGATE` (property delegate field) を取得。 */
    public val DELEGATE: IrDeclarationOrigin by lazy {
        lookup("DELEGATE")
    }

    private fun lookup(name: String): IrDeclarationOrigin {
        val cls = IrDeclarationOrigin::class.java

        // 形式 A: top-level interface/object に直接 static field がある (2.3.20+ 想定)
        runCatching {
            val field = cls.getField(name)
            @Suppress("UNCHECKED_CAST")
            return field.get(null) as IrDeclarationOrigin
        }

        // 形式 B: companion object 経由の getter (2.3.0 / 10 想定)
        runCatching {
            val companion = cls.getField("Companion").get(null)
            val getter = companion.javaClass.getMethod("get$name")
            return getter.invoke(companion) as IrDeclarationOrigin
        }

        // 形式 C: companion object のフィールド (将来の変更に備えて)
        runCatching {
            val companion = cls.getField("Companion").get(null)
            val field = companion.javaClass.getField(name)
            @Suppress("UNCHECKED_CAST")
            return field.get(companion) as IrDeclarationOrigin
        }

        error(
            "Cannot resolve IrDeclarationOrigin.$name on ${cls.name}. " +
                "Available companion methods: " + runCatching {
                    cls.getField("Companion").get(null).javaClass.methods.map { it.name }
                }.getOrDefault(emptyList<String>()),
        )
    }
}
