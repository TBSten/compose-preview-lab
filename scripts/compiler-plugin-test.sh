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

# Override the kotlin-compiler-embeddable version that kctfork drives via -Ptest.kotlin.
# compiler-plugin/build.gradle.kts feeds this into resolutionStrategy.force.
./gradlew \
    :compiler-plugin:test \
    --rerun-tasks \
    -Ptest.kotlin="$VERSION" \
    --continue 2>&1 | tee "$LOG"

echo "[compiler-plugin-test] $VERSION OK"
