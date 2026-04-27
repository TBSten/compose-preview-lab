#!/usr/bin/env bash
# `scripts/supported-kotlin-versions.txt` に列挙された全 Kotlin バージョンに対して
# smoke-test.sh を順次実行する。
#
# Usage: ./scripts/smoke-test-all.sh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

VERSIONS_FILE="scripts/supported-kotlin-versions.txt"

failed=()
while IFS= read -r v; do
    [[ -z "$v" || "$v" =~ ^[[:space:]]*# ]] && continue
    if ./scripts/smoke-test.sh "$v"; then
        :
    else
        failed+=("$v")
    fi
done < "$VERSIONS_FILE"

if (( ${#failed[@]} > 0 )); then
    echo "[smoke-all] FAILED: ${failed[*]}" >&2
    exit 1
fi

echo "[smoke-all] All versions passed."
