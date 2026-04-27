package me.tbsten.compose.preview.lab.compiler.compat

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar

/**
 * `CompilerPluginRegistrar.ExtensionStorage.registerExtension(companion, extension)` を
 * reflection 経由で呼び出す helper。
 *
 * Kotlin 2.4.0-Beta1 で `FirExtensionRegistrarAdapter.Companion` の親クラスが
 * `ProjectExtensionDescriptor` から `ExtensionPointDescriptor` に変わったため、
 * 直接呼ぶと `ClassCastException` が起きる。reflection で実行時にシグネチャを判定すれば
 * 2.3 / 2.4 両方で動作する。
 *
 * 参考: debuggable-compiler-plugin の `compat/ExtensionRegistration.kt`
 */
public fun CompilerPluginRegistrar.ExtensionStorage.registerExtensionCompat(companion: Any, extension: Any,) {
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
