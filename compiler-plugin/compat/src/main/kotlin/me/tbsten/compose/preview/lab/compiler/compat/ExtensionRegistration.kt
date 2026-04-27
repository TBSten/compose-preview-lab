package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar

/**
 * Reflection-based helper that calls
 * `CompilerPluginRegistrar.ExtensionStorage.registerExtension(companion, extension)`.
 *
 * In Kotlin 2.4.0-Beta1 the parent class of `FirExtensionRegistrarAdapter.Companion`
 * changed from `ProjectExtensionDescriptor` to `ExtensionPointDescriptor`, so calling
 * the method directly throws `ClassCastException`. Resolving the signature reflectively
 * at runtime keeps both Kotlin 2.3 and 2.4 working.
 *
 * Reference: `compat/ExtensionRegistration.kt` in debuggable-compiler-plugin.
 */
public fun CompilerPluginRegistrar.ExtensionStorage.registerExtensionCompat(companion: Any, extension: Any) {
    val storageClass = this::class.java
    val method = storageClass.methods.firstOrNull { m ->
        m.name == "registerExtension" &&
            m.parameterCount == 2 &&
            m.parameterTypes[0].isInstance(companion)
    } ?: error(
        "No compatible registerExtension(companion, extension) method on ${storageClass.name} " +
            "for companion type ${companion::class.java.name}",
    )
    method.invoke(this, companion, extension)
}
