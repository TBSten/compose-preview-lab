#!/usr/bin/env bash
# Run compiler-plugin-test.sh sequentially for every Kotlin version listed in
# `scripts/supported-kotlin-versions.txt`.
#
# Usage: ./scripts/compiler-plugin-test-all.sh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

VERSIONS_FILE="scripts/supported-kotlin-versions.txt"

failed=()
while IFS= read -r v; do
    [[ -z "$v" || "$v" =~ ^[[:space:]]*# ]] && continue
    if ./scripts/compiler-plugin-test.sh "$v"; then
        :
    else
        failed+=("$v")
    fi
done < "$VERSIONS_FILE"

if (( ${#failed[@]} > 0 )); then
    echo "[compiler-plugin-test-all] FAILED: ${failed[*]}" >&2
    exit 1
fi

echo "[compiler-plugin-test-all] All versions passed."
