package me.tbsten.compose.preview.lab.compiler.compat.k2320

import me.tbsten.compose.preview.lab.compiler.compat.CompatContext
import me.tbsten.compose.preview.lab.compiler.compat.k230.CompatContextImpl as K230CompatContextImpl

/**
 * Compatibility layer for Kotlin 2.3.20 (the FIR top-level decl gen stability point).
 *
 * `FirDeclarationGenerationExtension.getTopLevelClassIds` /
 * `getTopLevelCallableIds` were experimental on Kotlin 2.3.0–2.3.19 — registering the
 * per-`@Preview` hint generator on those versions crashed the FIR session at random
 * (the API was tagged `@ExperimentalTopLevelDeclarationsGenerationApi` and the
 * underlying caching contract shifted multiple times). Kotlin 2.3.20 is the version
 * where the API became stable enough to register without crashes, so this compat module
 * is the lower bound for [supportsFirHintGeneration].
 *
 * 2.3.21 still adds the KT-82395 fix for KLIB IC safety
 * ([supportsKlibCrossModuleHint]) — that gate stays on `compat-k2321`.
 *
 * All compiler-API behavior (FIR / IR / file-entry / annotations) is identical to
 * [K230CompatContextImpl] — only the `supportsFirHintGeneration` gate flips on. Every
 * other override is delegated.
 */
public class CompatContextImpl : CompatContext by K230CompatContextImpl() {

    override fun supportsFirHintGeneration(): Boolean = true

    // Kotlin 2.3.20 introduced `org.jetbrains.kotlin.fir.declarations.FirNamedFunction`,
    // the type referenced from `PreviewLabFirCheckersExtension.simpleFunctionCheckers`.
    // The class is now safe to load on this and later Kotlin versions, so we re-enable
    // checker registration. (compat-k2321 / compat-k240_beta2 inherit this `true` via
    // delegation.)
    override fun supportsFirCheckers(): Boolean = true

    public class Factory : CompatContext.Factory {
        override val minVersion: String = "2.3.20"
        override fun create(): CompatContext = CompatContextImpl()
    }
}
