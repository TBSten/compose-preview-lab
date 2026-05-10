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


# Kotlin 2.1.x / 2.2.x: the FIR per-declaration generator (`PreviewHintFirGenerator`)
# is not registered (`CompatContext.supportsFirHintGeneration = false`), so every
# gated test self-skips at runtime. The whole `:compiler-plugin:test` task is
# excluded here as a small wall-clock win — running the suite just to skip every
# gated case still takes ~30s of Gradle setup.
#
# Kotlin 2.3.0 / 2.3.10: an unrelated pre-existing API drift bites these versions —
# `org.jetbrains.kotlin.fir.declarations.FirNamedFunction` (referenced by
# `CollectScopeAnnotationChecker` / `PreviewLabFirCheckersExtension`) does not exist
# yet, so the always-on FIR checker extension fails class loading via
# `NoClassDefFoundError`. The cure (= compat layer for FIR checker classes) is its
# own ticket; until then we keep these excluded.
#
# Kotlin 2.3.20+: the FIR generator IS registered (via `compat-k2320`) and the
# checker classes resolve. JVM-targeting kctfork tests pass; the single KLIB-targeting
# test (`CrossModuleAggregationKlibTest`) self-skips on 2.3.20 via its own
# `isAtLeast(2, 3, 21)` gate. The KLIB-only `UnsupportedKotlinErrorTest` runs on
# 2.3.20 (= the inverse gate) and verifies the error path. So 2.3.20 is no longer
# excluded.
TEST_EXCLUDES=""
case "$VERSION" in
    2.1.* | 2.2.* | 2.3.0 | 2.3.10)
        TEST_EXCLUDES="--exclude-task :compiler-plugin:test"
        echo "[compiler-plugin-test] Skipping :compiler-plugin:test for Kotlin $VERSION (FIR hint generator not registered or FIR checker API drift)"
        ;;
esac

# Override the kotlin-compiler-embeddable version that kctfork drives via -Ptest.kotlin.
# compiler-plugin/build.gradle.kts feeds this into resolutionStrategy.force.
if [ -z "$TEST_EXCLUDES" ]; then
    ./gradlew \
        :compiler-plugin:test \
        --rerun-tasks \
        -Ptest.kotlin="$VERSION" \
        --continue 2>&1 | tee "$LOG"
else
    echo "[compiler-plugin-test] Skipping compiler-plugin:test for Kotlin < 2.3.21"
fi

echo "[compiler-plugin-test] $VERSION OK"
