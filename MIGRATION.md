# Migration Guide

This document captures notable behavior changes and the actions library users
need to take when upgrading. Each entry is keyed on the version that ships the
change.

---

## Cross-module preview aggregation rewrite (bug-017)

This release rewrites how `collectAllModulePreviews()` discovers previews from
dependency modules. The new path works on KLIB-based platforms (JS / WasmJS /
iOS) in addition to JVM, but it tightens a few requirements.

### TL;DR

| Change | Action required |
|---|---|
| `collectAllModulePreviews()` now requires Kotlin **2.3.21+** | Upgrade Kotlin, or replace the call with `collectModulePreviews()` (single-module) |
| All modules in the dependency graph must use the same plugin version | Upgrade every module that depends on Compose Preview Lab in lockstep |
| Each Kotlin module must have a unique module name within its compile classpath | Standard Gradle convention; only revisit if you hit a `KLIB IdSignature` error |
| `kotlin.incremental.js=true` + `collectAllModulePreviews()` requires Kotlin 2.3.21+ | Same as the first row; KT-82395 is fixed there |

### Why each restriction exists

#### `collectAllModulePreviews()` requires Kotlin 2.3.21+

The new aggregation pipeline emits per-module **marker classes** through the
FIR `FirDeclarationGenerationExtension` API. That API is stable for KLIB
top-level declaration generation only on Kotlin 2.3.21 and later
([KT-82395](https://youtrack.jetbrains.com/issue/KT-82395)). Older compilers
either crash on JS incremental builds or silently drop the synthetic
declarations.

If you are stuck on Kotlin 2.1.x – 2.3.20 and want preview aggregation:

```kotlin
// Single-module collection (no Kotlin floor change)
val previews by collectModulePreviews()

// Concatenate sibling modules manually
val allPreviews = uiLib.uiLibPreviews + featureA.featureAPreviews
```

#### All modules must use the same plugin version

The wire format the plugin generates changed (file-facade hint → marker class
+ FIR-emitted hint). A module compiled with an older plugin emits the old
shape, which a newer-plugin consumer cannot discover. This will manifest as
**silent drops** — no compile error, just missing previews from the older
module. Always upgrade every module on the classpath together.

#### Kotlin module name uniqueness

The marker class name is derived from a SHA-256 hash of `(Kotlin module name,
project root path)`. Two unrelated *published* artifacts that happen to share
a Kotlin module name still hash to different suffixes because their build
roots differ. Within a single Gradle project, sibling modules already have
unique Kotlin module names by convention (`projectName-uiLib`,
`projectName-featureA`, …) — no action needed unless you intentionally
override `moduleName` to collide.

If you do hit a collision, set a distinct `compilations.main.kotlinOptions.moduleName`
on each affected module.

#### JS incremental compile

The same Kotlin 2.3.21 floor applies. Below that version, the plugin's
top-level FIR declarations confuse the JS incremental cache and corrupt
re-builds. The plugin emits a hard error on builds that combine
`kotlin.incremental.js=true` (the default) with `collectAllModulePreviews()`
on an unsupported compiler.

### Error message you may see

```
e: collectAllModulePreviews() requires Kotlin 2.3.21 or later for cross-module
   preview aggregation. Either upgrade Kotlin to 2.3.21+, or replace
   collectAllModulePreviews() with collectModulePreviews() for single-module
   collection.
```

### Behind the scenes

- FIR pass emits `me.tbsten.compose.preview.lab.exports.PreviewLabExportMarker_<hash>`
  (an empty `interface` to avoid Compose `$stableprop` synthesis on JS IC, with
  `Modality.ABSTRACT` to satisfy Konan's vtable layout) plus a
  `previewLabExport(value: <Marker>): Unit` hint function in the same package.
- IR pass emits `previewLabAutoProvider_<hash>(): List<CollectedPreview>` whose
  body folds `@Preview` functions in the current module **and** transitive
  dependency previews discovered through other modules' hints
  (`distinctPreviewsById` dedups).
- Downstream `collectAllModulePreviews()` walks `referenceFunctions` for the
  fixed `previewLabExport` callable, picks each overload's marker-class
  parameter type, strips the `PreviewLabExportMarker_` prefix to recover the
  hash, and reconstructs the matching `previewLabAutoProvider_<hash>` to call.

That marker-class parameter type is the key trick: KLIB IdSignatures are
derived from `(name, parameter types)`, so each module's hint has a naturally
unique signature and KLIB linking does not collide.
