package me.tbsten.compose.preview.lab.compiler.compat.k2321

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.k2320.CompatContextImpl as K2320CompatContextImpl

/**
 * Compatibility layer for Kotlin 2.3.21 and later 2.3.x patches.
 *
 * Kotlin 2.3.21 ships the fix for KT-82395 (JS/Wasm incremental compile × top-level
 * declaration generation), which is the last blocker for KLIB-safe FIR-based hint
 * generation. From this version onward `collectAllModulePreviews()` can run on every
 * platform — see [supportsKlibCrossModuleHint].
 *
 * All compiler-API behavior is identical to 2.3.20; only the KLIB IC-safety gate
 * differs, so everything else is delegated to [K2320CompatContextImpl] (which in turn
 * delegates to `compat-k230` for the bulk of the FIR / IR / file-entry / annotation
 * methods, and overrides only [CompatContext.supportsFirHintGeneration]).
 */
public class CompatContextImpl : CompatContext by K2320CompatContextImpl() {

    override fun supportsKlibCrossModuleHint(): Boolean = true

    public class Factory : CompatContext.Factory {
        override val minVersion: String = "2.3.21"
        override fun create(): CompatContext = CompatContextImpl()
    }
}
