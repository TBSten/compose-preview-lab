package me.tbsten.compose.preview.lab.compiler.feature.previewCollection

import org.jetbrains.kotlin.name.FqName

/**
 * Package that owns every per-declaration hint function and marker interface
 * synthesized by the `previewCollection` feature.
 *
 * Cross-module discovery walks
 * `IrPluginContext.referenceFunctions(CallableId(HINT_PACKAGE, "previewHint_<scope>"))`,
 * so consumers learn about dependency-module previews purely by the package + per-scope
 * callable name — no per-hint inspection needed.
 *
 * Lives at the feature root because both the FIR-side generators
 * (`hintAndMarkerGeneration/`) and the IR-side discovery / body filler
 * (`collectPreviewsReplacement/`) reference it.
 */
internal val HINT_PACKAGE: FqName = FqName("me.tbsten.compose.preview.lab.hints")
