# Collection Scopes

> [日本語版](./collect-scopes.ja.md)

Detailed reference for the **`collectScopes`** feature on
[`@ComposePreviewLabOption`](../src/commonMain/kotlin/me/tbsten/compose/preview/lab/ComposePreviewLabOption.kt)
and its companion sentinel `DefaultCollectScope`. The annotation's KDoc carries a one-paragraph
summary; this document is the single source of truth for the design rationale, use cases, and
ABI semantics.

The whole scope feature is currently `@ExperimentalComposePreviewLabApi`-gated. The no-arg
`collectModulePreviews()` / `collectAllModulePreviews()` overloads remain stable; only the
`scope: String` overloads and the `collectScopes` annotation argument require `@OptIn`.

## Why `collectScopes` exists

`collectScopes` exists primarily to **keep a component library's previews out of a consumer
application's gallery**. Most projects pick one scope per module once and never override it
per-`@Preview`; the multi-element form is supported, but it is the minority case for previews
that legitimately belong in more than one bucket.

## Primary use case — library / app isolation (1 module : 1 scope)

A component library publishes its previews so its maintainers can iterate on them; the
library's downstream consumers (apps) do not want the library's previews showing up in their
own `collectAllModulePreviews()` call. The conventional pattern is one Gradle DSL line in the
library — every `@Preview` in that module is then automatically registered under the library
scope, with no per-`@Preview` annotation required.

```kotlin
// -- inside :ui-library / build.gradle.kts --
// (Gradle build scripts do not propagate Kotlin opt-in. The DSL property is marked
//  experimental for IDE/programmatic-access guidance only — the scripted call below
//  compiles without `@OptIn`.)
composePreviewLab {
    collectPreviews {
        defaultCollectScope = "acme_ui"
    }
}

// -- inside :ui-library / Button.kt --
@Preview                                 // no @ComposePreviewLabOption needed
@Composable
fun PrimaryButtonPreview() { PrimaryButton() }
// ↑ emitted under previewHint_acme_ui because of the DSL above.

// -- inside :ui-library / Previews.kt --
@file:OptIn(me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi::class)

val libraryGallery by collectAllModulePreviews(scope = "acme_ui")

// -- inside :app (depends on :ui-library) --
@Preview @Composable fun LoginScreenPreview() { LoginScreen() }   // → previewHint_default
val appGallery by collectAllModulePreviews()                      // sees LoginScreenPreview only
                                                                  // — PrimaryButtonPreview is hidden
                                                                  //   under "acme_ui".
```

The mechanism: the compiler plugin reads
`composePreviewLab.collectPreviews.defaultCollectScope` from the Gradle DSL and substitutes
any `[DefaultCollectScope]` (= `["default"]`) per-`@Preview` value with it. So you can leave
`@ComposePreviewLabOption.collectScopes` at its default everywhere and still get module-level
isolation.

The no-arg `collectAllModulePreviews()` overload remains stable and does not require `@OptIn`;
only the `scope: String` overload (and the `collectScopes` annotation argument used to
populate it) is currently experimental.

## Secondary use case — one preview registered in multiple scopes

When a single preview legitimately belongs in more than one bucket (e.g. a "showcase" gallery
that re-uses pieces from the library's own gallery), the per-`@Preview` annotation form takes
over and lists every scope explicitly.

```kotlin
@file:OptIn(me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi::class)

@ComposePreviewLabOption(collectScopes = ["acme_ui", "showcase"])
@Preview @Composable
fun ButtonShowcase() { PrimaryButton() }

val libraryGallery by collectAllModulePreviews(scope = "acme_ui")   // sees it
val showcaseGallery by collectAllModulePreviews(scope = "showcase") // also sees it
```

Each entry must match `[A-Za-z0-9_]+` because the compiler plugin embeds it into the
synthetic hint function name (`previewHint_<scope>`); the FIR `CollectScopeAnnotationChecker`
reports invalid values per element at analysis time, before IR generation, so the error
surfaces in the IDE highlighter. `ignore = true` always wins, regardless of how many scopes
are listed.

## Resolution order at compile time

Per `@Preview` and per `collect[All]ModulePreviews` call:

1. If the explicit annotation argument or call-site argument is anything other than
   `[DefaultCollectScope]` / `DefaultCollectScope`, it wins as-is.
2. Otherwise, the compiler plugin substitutes the module's
   `composePreviewLab.collectPreviews.defaultCollectScope` (set via the Gradle DSL) so a
   library can pin all of its previews to a library-specific bucket without annotating each
   `@Preview`.
3. If the Gradle DSL was not set either, the runtime default `"default"` applies.

## Experimental marker visibility — BCV quirk

The `collectScopes` property is marked with `@property:ExperimentalComposePreviewLabApi` so
the use-site requires opt-in. Be aware that BCV's `nonPublicMarkers` filter only suppresses
this property in the **KLIB** baseline; the **JVM** and **Android** baselines still record
the abstract getter because Kotlin attaches `@property:` markers to a synthetic
`<name>$annotations()` helper rather than the getter itself, and BCV's method-level
annotation scan does not pick that up. The use-site qualifiers `@get:` / `@field:` /
`@param:` are forbidden on `@RequiresOptIn` markers by Kotlin's opt-in mechanism, so the
asymmetry cannot be fixed from the Kotlin source side alone. Treat the eventual
experimental→stable promotion of `collectScopes` as a **release-notes / KDoc concern**, not
a baseline-diff signal.

### Why both the companion *and* the const carry the experimental marker

The `Companion` object and `DefaultCollectScope` each carry
`@ExperimentalComposePreviewLabApi` for **complementary** reasons:

- The companion-level marker keeps the `Companion` *class* itself out of every BCV baseline
  (KLIB/JVM/Android), so an empty companion does not lock in as ABI if the const ever moves
  elsewhere. Without it, the empty `Companion` class signature stays in `*.api` /
  `*.klib.api` baselines forever even after we eventually remove or move
  `DefaultCollectScope`, locking in the `Companion` class as ABI surface even though it has
  no stable members.
- The const-level marker keeps the const *member* itself filtered: on KLIB this nests with
  the companion-level filter, and on JVM/Android the const surfaces directly as a `public
  static final field` on the outer `ComposePreviewLabOption` class (the companion-level
  annotation does **not** propagate to that flattened field — see the root build.gradle.kts
  Known Limitation note for details). Without this marker the field stays in JVM/Android
  baselines forever.
