#!/usr/bin/env bash
# Nightly Checking ワークフロー用 Discord ペイロード組み立てスクリプト。
# 環境変数:
#   SEND_PBT  / SEND_EXP    : "true" のときに各セクションを含める
#   PBT_RESULT / EXP_RESULT : needs.<job>.result
#   EXP_COUNT               : exploratory ジョブが報告した issue 件数
#   ACTIONS_URL             : Actions Run の URL
#   ISSUES_DIR              : .local/nightly-exploration/issues
#   PAYLOAD_DIR             : 出力先 (main.txt + chunk_NN.txt を生成)
#
# 出力 (PAYLOAD_DIR 配下):
#   main.txt      : 1 通目の本文 (Discord メッセージは 2000 文字制限なので 1900 で分割)
#   chunk_02.txt  : 2 通目以降 (同じチャンネルへ連投)
#
# 探索的テストの各 issue は ".local/nightly-exploration/issues/NN-slug.md" にあり、
# 先頭の H1 タイトルと `**カテゴリ**` / `**重要度**` 行、`## 詳細` 直後の数行を抽出する。

set -euo pipefail

# 日本語混じりの本文を「文字数」ベースで分割するため UTF-8 ロケールを明示する。
# C.UTF-8 が無い環境のため LANG にも UTF-8 を指定する。
export LC_ALL=${LC_ALL:-C.UTF-8}
export LANG=${LANG:-C.UTF-8}

: "${PAYLOAD_DIR:=.local/nightly-exploration/payload}"
: "${ISSUES_DIR:=.local/nightly-exploration/issues}"
: "${SEND_PBT:=false}"
: "${SEND_EXP:=false}"
: "${PBT_RESULT:=}"
: "${EXP_RESULT:=}"
: "${EXP_COUNT:=0}"
: "${ACTIONS_URL:=}"

CHUNK_LIMIT=1900
TMP_RAW="$(mktemp)"
trap 'rm -f "$TMP_RAW"' EXIT

mkdir -p "$PAYLOAD_DIR"
rm -f "$PAYLOAD_DIR"/main.txt "$PAYLOAD_DIR"/chunk_*.txt

write_pbt_section() {
  printf '# PBT\n'
  printf 'ステータス: %s\n' "$PBT_RESULT"
  if [ -n "$ACTIONS_URL" ]; then
    printf 'Actions: %s\n' "$ACTIONS_URL"
  fi
  printf '\n'
}

write_exp_header() {
  printf '# 探索的テスト\n'
  if [ "$EXP_RESULT" = "failure" ]; then
    printf '(探索ジョブが失敗または途中終了しています。部分結果を表示します)\n'
  fi
  printf '発見件数: %s件\n' "$EXP_COUNT"
  if [ -n "$ACTIONS_URL" ]; then
    printf 'Actions: %s\n' "$ACTIONS_URL"
  fi
  printf '\n'
}

write_exp_issues() {
  if [ ! -d "$ISSUES_DIR" ]; then
    return 0
  fi
  local idx=0
  local f title meta detail
  while IFS= read -r f; do
    idx=$((idx + 1))
    title=$(awk 'NR==1 || /^# /{ sub(/^# +/, ""); print; exit }' "$f" || true)
    if [ -z "$title" ]; then
      title="(タイトル未設定)"
    fi
    meta=$(awk '/^\*\*(カテゴリ|重要度)\*\*:/{print}' "$f" | head -2 | paste -sd ' / ' -)
    detail=$(awk '
      /^## 詳細[[:space:]]*$/ { capture=1; next }
      /^## / && capture { exit }
      capture && NF { print }
    ' "$f" | head -3 | tr '\n' ' ' | cut -c1-200)

    printf '## 探索的テスト %d. %s\n' "$idx" "$title"
    if [ -n "$meta" ]; then
      printf '%s\n' "$meta"
    fi
    if [ -n "$detail" ]; then
      printf '%s\n' "$detail"
    fi
    printf '\n'
  done < <(find "$ISSUES_DIR" -maxdepth 1 -type f -name '*.md' | sort)
}

{
  if [ "$SEND_PBT" = "true" ]; then
    write_pbt_section
  fi
  if [ "$SEND_EXP" = "true" ]; then
    write_exp_header
    write_exp_issues
  fi
} > "$TMP_RAW"

if [ ! -s "$TMP_RAW" ]; then
  echo "No content to send (SEND_PBT=$SEND_PBT, SEND_EXP=$SEND_EXP). Skipping."
  exit 0
fi

chunk_idx=0
current=""

flush_chunk() {
  chunk_idx=$((chunk_idx + 1))
  local target
  if [ "$chunk_idx" -eq 1 ]; then
    target="$PAYLOAD_DIR/main.txt"
  else
    target=$(printf '%s/chunk_%02d.txt' "$PAYLOAD_DIR" "$chunk_idx")
  fi
  printf '%s' "$current" > "$target"
  current=""
}

while IFS= read -r line || [ -n "$line" ]; do
  candidate_len=$(( ${#current} + ${#line} + 1 ))
  if [ "$candidate_len" -gt "$CHUNK_LIMIT" ] && [ -n "$current" ]; then
    flush_chunk
  fi
  if [ -n "$current" ]; then
    current+=$'\n'
  fi
  current+="$line"
done < "$TMP_RAW"

if [ -n "$current" ]; then
  flush_chunk
fi

echo "Wrote $chunk_idx chunk file(s) to $PAYLOAD_DIR"
ls -la "$PAYLOAD_DIR"
