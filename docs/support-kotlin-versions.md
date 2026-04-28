# Supported Kotlin Versions

The compose-preview-lab compiler-plugin supports several Kotlin compiler versions from a
single published artifact by bundling per-version compat modules into a shadow JAR and
dispatching at runtime via ServiceLoader.

## Current support matrix

The supported range tracks `scripts/supported-kotlin-versions.txt` (the SSOT for CI).

| Kotlin              | Status | Notes                                                                                          |
| ------------------- | :----: | ---------------------------------------------------------------------------------------------- |
| 2.0.x / 2.1.0–2.1.10|   ❌   | Out of scope: requires the unified `IrMemberAccessExpression.arguments` API (2.1.20+)           |
| 2.1.20 / 2.1.21     |   ✅   | `:compiler-plugin:compat-k210` ships legacy `IrBuilderWithScope`-receiver IR builders           |
| 2.2.0 – 2.2.10      |   ✅   | `:compiler-plugin:compat-k222` absorbs the `IrBuilder` receiver widening                        |
| 2.2.20 / 2.2.21     |   ✅   | `:compiler-plugin:compat-k2220` (incremental delta over `compat-k222`)                          |
| 2.3.0 / 2.3.10      |   ✅   | `:compiler-plugin:compat-k230` + `IrDeclarationOriginCompat` reflection helper                  |
| 2.3.20 / 2.3.21     |   ✅   | Same `compat-k230`; project pinned to 2.3.21 via `gradle/libs.versions.toml`                    |
| 2.4.0-Beta2         |   ✅   | `:compiler-plugin:compat-k240_beta2` swaps in `IrAnnotationImpl`; `IrAnnotationCompat` covers `getAnnotation` |

The single source of truth that the CI matrix and test scripts read:
[`scripts/supported-kotlin-versions.txt`](../scripts/supported-kotlin-versions.txt).

## Architecture

```
compiler-plugin/                        # main (version-agnostic) — published as a shadow JAR
├── compat/                             # shared SPI: CompatContext, KotlinToolingVersion, ServiceLoader
├── compat-k210/                        # Kotlin 2.1.20+: legacy IrBuilderWithScope receivers
├── compat-k222/                        # Kotlin 2.2.x: IrBuilder receiver widening absorbed
├── compat-k2220/                       # Kotlin 2.2.20+: incremental delta over k222
├── compat-k230/                        # Kotlin 2.3.x: FirFunction + IrConstructorCallImpl
└── compat-k240_beta2/                  # Kotlin 2.4+: IrAnnotationImpl (handles the annotations type change)
```

At build time the `compiler-plugin` shadow JAR pulls in `compiler-plugin/compat` plus each
`compiler-plugin/compat-k*`, and merges
`META-INF/services/me.tbsten.compose.preview.lab.compiler.compat.CompatContext$Factory`.

At runtime:
1. `CompatContext.load()` enumerates every factory via `ServiceLoader.load(CompatContext.Factory)`.
2. The current Kotlin compiler version is read from `META-INF/compiler.version`.
3. Among factories whose `minVersion <= currentVersion`, the one with the largest `minVersion`
   is chosen.

## Adding a new Kotlin version

### A) Patch release without API drift (e.g. 2.3.30)

1. Append a line to `scripts/supported-kotlin-versions.txt`.
2. Verify locally with `./scripts/compiler-plugin-test.sh 2.3.30`.
3. Open the PR. The CI matrix reads the SSOT, so the new version is exercised automatically.

### B) Minor / major release with API drift

1. Create a new `compiler-plugin/compat-kXYZ/` module:
   - Put `X.Y.Z` in `version.txt`.
   - Pin `compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:X.Y.Z")` in `build.gradle.kts`.
   - Implement `src/main/kotlin/.../compat/kXYZ/CompatContextImpl.kt`:
     - Delegate to the closest existing compat module (`CompatContext by k230.CompatContextImpl()`)
       and only override the diff.
   - Register `src/main/resources/META-INF/services/...$Factory`.
2. Add `include(":compiler-plugin:compat-kXYZ")` to `settings.gradle.kts`.
3. Add `add(embedded.name, projects.compilerPlugin.compatKxyz)` to the `embedded` configuration
   in `compiler-plugin/build.gradle.kts`. Note that the typesafe accessor is camel-cased: e.g.
   `compat-k230` → `projects.compilerPlugin.compatK230`, `compat-k240_beta2` → `projects.compilerPlugin.compatK240Beta2`.
4. Append the new version to `scripts/supported-kotlin-versions.txt`.
5. Verify with `./scripts/compiler-plugin-test.sh X.Y.Z`.
6. Open the PR.

### Reference implementations (cloned locally)

- Metro: `.local/tmp/metro/compiler-compat/` (`CompatContext`, `KotlinToolingVersion`, k* sub-modules)
- debuggable-compiler-plugin: `.local/tmp/debuggable-compiler-plugin/debuggable-compiler/compat/`
  (3-layer test strategy / compiler-plugin-test scripts)

## Binary incompatibilities absorbed via reflection

The accessor shape of `IrDeclarationOrigin` shifts across Kotlin 2.3 patches
(`Companion.getX()` on 2.3.0/10 vs. a static field GET on 2.3.20+).
[`IrDeclarationOriginCompat`](../compiler-plugin/compat/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/compat/IrDeclarationOriginCompat.kt)
resolves these (`LOCAL_FUNCTION_FOR_LAMBDA`, `DELEGATE`) at runtime.
Add a new lookup there whenever another `IrDeclarationOrigin` member is needed.

`IrUtilsKt.getAnnotation(...)` had its return type changed in Kotlin 2.4. That is absorbed by
[`IrAnnotationCompat`](../compiler-plugin/compat/src/main/kotlin/me/tbsten/compose/preview/lab/compiler/compat/IrAnnotationCompat.kt),
which walks `IrAnnotationContainer.annotations` directly.
