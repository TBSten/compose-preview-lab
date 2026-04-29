package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin

/**
 * Helper that accesses `IrDeclarationOrigin.<NAME>` in a way that does not depend on
 * the Kotlin patch version.
 *
 * Across Kotlin 2.3 patches the declaration of `IrDeclarationOrigin` itself changes:
 * - 2.3.0 / 2.3.10: defined inside `companion object`, so the bytecode call site is
 *   `Companion.getLOCAL_FUNCTION_FOR_LAMBDA()`.
 * - 2.3.20+: defined as a member of the top-level object, so the bytecode call site
 *   is a static field GET.
 *
 * Writing `IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA` directly leads to
 * `NoSuchMethodError` whenever the runtime jar disagrees with the API the plugin was
 * compiled against. This helper tries both shapes via reflection and returns whichever
 * one resolves.
 */
public object IrDeclarationOriginCompat {
    /**
     * Resolves `LOCAL_FUNCTION_FOR_LAMBDA`, supporting both 2.3.0/10 (companion accessor)
     * and 2.3.20+ (static field).
     */
    public val LOCAL_FUNCTION_FOR_LAMBDA: IrDeclarationOrigin by lazy {
        lookup("LOCAL_FUNCTION_FOR_LAMBDA")
    }

    /** Resolves `DELEGATE` (the property delegate field origin). */
    public val DELEGATE: IrDeclarationOrigin by lazy {
        lookup("DELEGATE")
    }

    /**
     * Resolves `IR_EXTERNAL_DECLARATION_STUB` (the marker on declarations loaded from
     * external compiled dependencies, used to filter classpath-discovered hints).
     */
    public val IR_EXTERNAL_DECLARATION_STUB: IrDeclarationOrigin by lazy {
        lookup("IR_EXTERNAL_DECLARATION_STUB")
    }

    /**
     * Resolves `DEFINED` (the standard origin for user-defined declarations).
     *
     * Across Kotlin patches the bytecode return type of `getDEFINED()` differs:
     * - 2.1.x / 2.2.x: returns `IrDeclarationOriginImpl`
     * - 2.3.x+: returns `IrDeclarationOrigin`
     *
     * Writing `IrDeclarationOrigin.DEFINED` directly causes `NoSuchMethodError` when the
     * compiled bytecode signature does not match the runtime jar. This helper resolves it
     * reflectively against whatever shape the runtime exposes.
     */
    public val DEFINED: IrDeclarationOrigin by lazy {
        lookup("DEFINED")
    }

    private fun lookup(name: String): IrDeclarationOrigin {
        val cls = IrDeclarationOrigin::class.java

        // Shape A: a static field on the top-level interface/object (expected on 2.3.20+).
        runCatching {
            val field = cls.getField(name)
            @Suppress("UNCHECKED_CAST")
            return field.get(null) as IrDeclarationOrigin
        }

        // Shape B: getter on the companion object (expected on 2.3.0 / 2.3.10).
        runCatching {
            val companion = cls.getField("Companion").get(null)
            val getter = companion.javaClass.getMethod("get$name")
            return getter.invoke(companion) as IrDeclarationOrigin
        }

        // Shape C: field on the companion object (kept as a forward-compatibility fallback).
        runCatching {
            val companion = cls.getField("Companion").get(null)
            val field = companion.javaClass.getField(name)
            @Suppress("UNCHECKED_CAST")
            return field.get(companion) as IrDeclarationOrigin
        }

        error(
            "Cannot resolve IrDeclarationOrigin.$name on ${cls.name}. " +
                "Available companion methods: " +
                runCatching {
                    cls.getField("Companion").get(null).javaClass.methods.map { it.name }
                }.getOrDefault(emptyList<String>()),
        )
    }
}
