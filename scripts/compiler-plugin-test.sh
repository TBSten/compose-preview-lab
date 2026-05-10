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
# is not registered (`CompatContext.supportsFirHintGeneration = false`) and the FIR
# checker extension is also gated off (`CompatContext.supportsFirCheckers = false`,
# `FirNamedFunction` does not exist on these versions). Every test under
# `:compiler-plugin:test` is therefore either ungated (= would run) or gated and
# self-skipping; running the task adds ~30s of Gradle setup for negligible coverage
# gain, so we exclude it here for wall-clock efficiency.
#
# Kotlin 2.3.0 / 2.3.10: same as above — `supportsFirHintGeneration = false` (FIR
# generator stable from 2.3.20) and `supportsFirCheckers = false` (`FirNamedFunction`
# introduced in 2.3.20). The compat-layer split keeps the JVM classloader away from
# `PreviewLabFirCheckersExtension`, so the plugin starts successfully and ungated
# contract tests run (= verifies plugin registration itself works on these versions),
# but every gated test still self-skips. The task is included in the matrix to keep
# the registration regression signal active.
#
# Kotlin 2.3.20+: the FIR generator IS registered (via `compat-k2320`) and the
# checker classes resolve. JVM-targeting kctfork tests pass; the single KLIB-targeting
# test (`CrossModuleAggregationKlibTest`) self-skips on 2.3.20 via its own
# `isAtLeast(2, 3, 21)` gate. The KLIB-only `UnsupportedKotlinErrorTest` runs on
# 2.3.20 (= the inverse gate) and verifies the error path.
TEST_EXCLUDES=""
case "$VERSION" in
    2.1.* | 2.2.*)
        TEST_EXCLUDES="--exclude-task :compiler-plugin:test"
        echo "[compiler-plugin-test] Skipping :compiler-plugin:test for Kotlin $VERSION (no actionable coverage — FIR hint generator + checker both gated off)"
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
