package me.tbsten.compose.preview.lab.compiler.utils.ir

import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.isWasm
import org.jetbrains.kotlin.platform.jvm.isJvm

/**
 * Whether the IR pass is running on a target whose cross-module dependency resolution
 * goes through the KLIB linker (and therefore the KLIB incremental-compile cache).
 * Returns `true` for JS / Wasm JS / Native (= iOS, macOS, Linux, …); `false` for JVM
 * (and Android, which targets JVM bytecode).
 *
 * **Why this matters**: `IrPluginContext.referenceFunctions(...)` is the cross-module
 * hint discovery API and works on every target, but on KLIB-based platforms a known
 * incremental-compile bug ([KT-82395](https://youtrack.jetbrains.com/issue/KT-82395))
 * leaks stale top-level declarations through the KLIB IC cache. The fix landed in
 * Kotlin 2.3.21, so on KLIB targets we additionally gate on
 * `CompatContext.supportsKlibCrossModuleHint()` (= 2.3.21+) before walking dep-module
 * hints. JVM / Android consumers don't have the IC issue and can run the discovery on
 * any Kotlin version that supports `CompatContext.supportsFirHintGeneration()`
 * (= 2.3.20+).
 *
 * `Native` is not in `kotlin.platform` as a single helper, so we use an explicit
 * `isJs() || isWasm() || isNative()` allowlist to identify KLIB-based platforms (after
 * short-circuiting on JVM / null). Anything that hits the allowlist is treated as
 * KLIB-based for our gate. Android Native (NDK) is captured by `isNative()`, which is
 * correct (Android Native targets are Konan-based).
 *
 * **Sample**: `pluginContext.platform.requiresKlibIcSafetyForCrossModuleHint`
 * - JVM module → `false`
 * - Android module (JVM-targeting) → `false`
 * - iOS / macOS Native module → `true`
 * - JS module → `true`
 */
internal val TargetPlatform?.requiresKlibIcSafetyForCrossModuleHint: Boolean
    get() {
        if (this == null) return false
        if (isJvm()) return false
        // Treat anything non-JVM as KLIB-based: covers JS / Wasm / Native (iOS / macOS / …)
        return isJs() || isWasm() || isNative()
    }

/**
 * Local approximation of `TargetPlatform?.isNative()` — the standard helper lives
 * in `org.jetbrains.kotlin.platform.konan` (`NativePlatform.kt`) which is part of the
 * `compiler:cli-common` artifact and therefore not always on the plugin's compile
 * classpath. Walk `componentPlatforms` and check each against the abstract
 * `org.jetbrains.kotlin.platform.NativePlatform` base class instead — the abstract
 * class is in the always-on `kotlin.platform` package.
 */
private fun TargetPlatform.isNative(): Boolean = componentPlatforms.any { it is org.jetbrains.kotlin.platform.NativePlatform }
