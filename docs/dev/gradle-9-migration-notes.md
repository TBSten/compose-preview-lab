# Gradle 9 / AGP 9 migration notes

Tracks the state of the upcoming `Gradle 8.14.x + AGP 8.9.x` → `Gradle 9.x + AGP 9.x` jump
for compose-preview-lab. This document is the developer-facing companion to ticket
[`task-033`](../../.local/ticket/task-033-agp-gradle-major-version-lag.md) (local only) and
ticket [`task-030`](../../.local/ticket/) (wrapper alignment).

The actual deprecation log dumps used to compile this note live under
`.local/tmp/task-033-*.log` (gitignored) and are regenerated with:

```bash
./gradlew help    --warning-mode all > .local/tmp/task-033-warning-mode-all.log     2>&1
./gradlew assemble --warning-mode all > .local/tmp/task-033-assemble-warning-mode.log 2>&1
./gradlew apiCheck --warning-mode all --continue > .local/tmp/task-033-apicheck-warning-mode.log 2>&1
```

## Current pinned versions

| Component                       | Pinned in repo                                | Upstream stable (2026-05-12) |
| ------------------------------- | --------------------------------------------- | ---------------------------- |
| Gradle wrapper                  | `gradle-8.14.5-bin.zip`                       | `9.5.0`                      |
| Android Gradle Plugin           | `agp = "8.9.3"` in `gradle/libs.versions.toml` | `9.2.0` (min Gradle 9.4.1, JDK 17) |
| Kotlin Gradle Plugin (project)  | `kotlin = "2.3.21"` (kotlin-dsl expects `2.0.21`) | n/a — managed per matrix |

Notes:

- nightly PBT matrix already pins `Kotlin 2.4.0-Beta2` against `gradle_version: 9.4.1`, so
  the Gradle 9 happy-path is exercised in CI on a probe axis even before the wrapper bumps.
- The Gradle wrapper was already advanced from `8.13` → `8.14.5` (task-030 progress); the
  task-033 ticket text referring to `8.13` is stale and is corrected here.

## Gradle 9 blockers observed today

All three Gradle 9 blocker deprecations during `./gradlew assemble --warning-mode all` are
emitted by AGP-decorated DSL classes, not by anything this repo writes:

| # | Reported during         | Decorated property                        | Owning AGP class                                                    |
| - | ----------------------- | ----------------------------------------- | ------------------------------------------------------------------- |
| 1 | `Configure project :annotation` | `isCrunchPngs: Boolean`           | `com.android.build.gradle.internal.dsl.BuildType$AgpDecorated`      |
| 2 | `Configure project :annotation` | `isUseProguard: Boolean`          | `com.android.build.gradle.internal.dsl.BuildType`                   |
| 3 | `Configure project :dev`        | `isWearAppUnbundled: Boolean`     | `com.android.build.api.variant.impl.ApplicationVariantImpl`         |

Each message ends with:
> *Starting with Gradle 9.0, this property will be ignored by Gradle. … will become unsupported in future versions of Groovy.*

Verifying these are upstream-owned and not in our build scripts:

```bash
grep -rn -E "isCrunchPngs|isUseProguard|isWearAppUnbundled" \
  buildLogic/ build.gradle.kts settings.gradle.kts dev/ annotation/
# (no matches)
```

→ These vanish only when we move to AGP 9.x, where the `getCrunchPngs() / setCrunchPngs(boolean)`
JavaBean-style accessors replace the `is-`-prefix Groovy variants. **No in-repo fix is
possible in this PR.**

## Build-script-level findings (in-repo)

| Source                         | Status                                                                                  |
| ------------------------------ | --------------------------------------------------------------------------------------- |
| `buildLogic/` (convention plugins) | Clean — no Gradle 9 deprecation triggered                                          |
| Root `build.gradle.kts`        | Clean                                                                                   |
| `settings.gradle.kts`          | Clean (`enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` remains valid in Gradle 9)  |
| `gradle-plugin/build.gradle.kts` | Emits an *informational* `Unsupported Kotlin plugin version` warning because `kotlin-dsl` embeds `2.0.21` while the project pins `2.3.21`. Already mitigated by manually setting `apiVersion`/`languageVersion = 2.1` on `KotlinCompile` tasks (see the inline comments referencing <https://kotl.in/gradle/kotlin-dsl-version-compatibility>). |

No in-repo property accessor, Provider API, or task DSL flagged by `--warning-mode all`.

## Adjacent (non Gradle 9) deprecations spotted in the same logs

Tracked separately, not blocking Gradle 9 migration:

- **Compose Multiplatform** `androidx.compose.ui.platform.ClipboardManager` /
  `LocalClipboardManager` are deprecated in favour of `Clipboard` / `LocalClipboard`
  (suspend-friendly). Hits `preview-lab/src/{wasmJsMain,jsMain,webMain}/.../CopyUserHandler*.kt`
  and `.../PreviewLabHeader.web.kt`. Belongs to the Compose 1.8+ migration story.
- **Kotlin K2** still reports `'expect'/'actual' classes are in Beta` (KT-61573) for
  `CopyUserHandler` actuals. Suppressed by `-Xexpect-actual-classes` when we choose to.
- **JDK native warning**
  `WARNING: A restricted method in java.lang.System has been called` originates from
  Gradle's bundled `native-platform-0.22-milestone-28.jar`. Goes away with a future Gradle
  bump; we cannot patch.

## Migration plan

1. **This PR (audit only)** — record the above; do not change `libs.versions.toml`.
2. **Wrapper finalisation (task-030 follow-up)** — confirm `gradle-wrapper.properties` is
   on the latest `8.14.x` patch before we cross the 9.x boundary.
3. **AGP 9.x bump PR** — separate change that:
   - bumps `agp = "9.2.0"` (or whatever is current),
   - lifts the wrapper to `9.4.1+`,
   - raises the JDK toolchain baseline to 17 (already configured via
     `libs.versions.jvmToolchain` — verify on bump),
   - re-runs `./gradlew help assemble apiCheck --warning-mode all` and expects the three
     `is-` Boolean deprecations above to be gone.
4. **CI hardening** — the existing `Kotlin 2.4.0-Beta2 + Gradle 9.4.1` PBT axis becomes the
   default once the wrapper crosses; consider promoting it from probe to required gate.

## Re-running the audit

The two diagnostic commands above are the canonical entry points. Output lives in
`.local/tmp/` and is gitignored — re-run, inspect, then update this document if the
in-repo / upstream split changes.
