#!/usr/bin/env bash
# Run the compiler-plugin tests against a single Kotlin version.
#
# Usage: ./scripts/smoke-test.sh <kotlin-version>
# Example: ./scripts/smoke-test.sh 2.3.21
set -euo pipefail

VERSION="${1:?Usage: smoke-test.sh <kotlin-version> (e.g. 2.3.21, 2.4.0-Beta2)}"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

LOG_DIR=".local/tmp"
mkdir -p "$LOG_DIR"
LOG="$LOG_DIR/smoke-${VERSION}-$(date +%s).log"

echo "[smoke] === Kotlin $VERSION ==="
echo "[smoke] log: $LOG"

# Override the kotlin-compiler-embeddable version that kctfork drives via -Ptest.kotlin.
# compiler-plugin/build.gradle.kts feeds this into resolutionStrategy.force.
./gradlew \
    :compiler-plugin:test \
    --rerun-tasks \
    -Ptest.kotlin="$VERSION" \
    --continue 2>&1 | tee "$LOG"

echo "[smoke] $VERSION OK"
