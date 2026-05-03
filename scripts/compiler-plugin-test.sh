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

# For Kotlin < 2.3.21, skip cross-module aggregation tests
# because they require FIR-based hint generation support.
TEST_EXCLUDES=""
case "$VERSION" in
    2.1.* | 2.2.* | 2.3.0 | 2.3.10 | 2.3.20)
        TEST_EXCLUDES="--exclude-task :compiler-plugin:test"
        echo "[compiler-plugin-test] Skipping all tests for Kotlin $VERSION (unsupported by collectAllModulePreviews)"
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
