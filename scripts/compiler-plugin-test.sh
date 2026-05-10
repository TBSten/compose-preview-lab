#!/usr/bin/env bash
# Run the compiler-plugin tests against a single Kotlin version.
#
# Usage: ./scripts/compiler-plugin-test.sh <kotlin-version>
# Example: ./scripts/compiler-plugin-test.sh 2.3.21
set -euo pipefail

VERSION="${1:?Usage: compiler-plugin-test.sh <kotlin-version> (e.g. 2.3.21, 2.4.0-Beta2)}"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

LOG_DIR=".local/tmp"
mkdir -p "$LOG_DIR"
LOG="$LOG_DIR/compiler-plugin-test-${VERSION}-$(date +%s).log"

echo "[compiler-plugin-test] === Kotlin $VERSION ==="
echo "[compiler-plugin-test] log: $LOG"


# All supported Kotlin versions run `:compiler-plugin:test`. The compat-layer
# split keeps the JVM classloader away from extension classes that don't exist
# on a given version (e.g. `PreviewLabFirCheckersExtension` on Kotlin <2.3.20),
# so the plugin starts successfully on every version in the matrix and ungated
# contract tests verify plugin registration itself works. Gated tests
# (FIR hint generator / KLIB IC) self-skip on versions that don't meet their
# `isAtLeast(...)` threshold:
#
# - Kotlin 2.1.x / 2.2.x: `supportsFirHintGeneration = false` and
#   `supportsFirCheckers = false`. Almost every feature test self-skips;
#   ungated contract tests still run.
# - Kotlin 2.3.0 / 2.3.10: same as above — `FirNamedFunction` introduced in
#   2.3.20 and FIR generator stable from 2.3.20.
# - Kotlin 2.3.20+: the FIR generator IS registered (via `compat-k2320`) and
#   the checker classes resolve. JVM-targeting kctfork tests pass; the single
#   KLIB-targeting test (`CrossModuleAggregationKlibTest`) self-skips on 2.3.20
#   via its own `isAtLeast(2, 3, 21)` gate. The KLIB-only
#   `UnsupportedKotlinErrorTest` runs on 2.3.20 (= the inverse gate) and
#   verifies the error path.

# Override the kotlin-compiler-embeddable version that kctfork drives via -Ptest.kotlin.
# compiler-plugin/build.gradle.kts feeds this into resolutionStrategy.force.
./gradlew \
    :compiler-plugin:test \
    --rerun-tasks \
    -Ptest.kotlin="$VERSION" \
    --continue 2>&1 | tee "$LOG"

echo "[compiler-plugin-test] $VERSION OK"
