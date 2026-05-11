#!/usr/bin/env bash
# Nightly Checking ワークフロー用 Discord 連投スクリプト。
# 環境変数:
#   DISCORD_WEBHOOK_URL : Discord webhook URL (必須)
#   ACTIONS_URL         : Actions Run の URL (フォールバック通知用)
#   PAYLOAD_DIR         : build-nightly-discord-payload.sh が生成した main.txt / chunk_*.txt の置き場所
#
# Discord メッセージの文字数上限 (2000) を超えないようにペイロードを 1990 文字以内に
# truncate しつつ、main.txt → chunk_*.txt の順に同じチャンネルへ連投する。

set -euo pipefail

: "${PAYLOAD_DIR:=.local/nightly-exploration/payload}"
: "${ACTIONS_URL:=}"

if [ -z "${DISCORD_WEBHOOK_URL:-}" ]; then
  echo "DISCORD_WEBHOOK_URL is empty; skipping Discord notification."
  exit 0
fi

post_content() {
  local content="$1"
  if [ -z "$content" ]; then
    return 0
  fi
  if [ "${#content}" -gt 1990 ]; then
    content="${content:0:1985}..."
  fi
  local payload
  payload=$(jq -n --arg c "$content" '{content: $c, allowed_mentions: {parse: []}}')
  local tmp_resp
  tmp_resp=$(mktemp)
  local http_code
  http_code=$(curl -sS -o "$tmp_resp" -w '%{http_code}' \
    -H 'Content-Type: application/json' \
    -X POST "$DISCORD_WEBHOOK_URL" \
    -d "$payload" || echo "000")
  if [ "$http_code" != "204" ] && [ "$http_code" != "200" ]; then
    echo "Discord POST failed: HTTP $http_code"
    cat "$tmp_resp" || true
    rm -f "$tmp_resp"
    return 1
  fi
  rm -f "$tmp_resp"
}

if [ ! -f "$PAYLOAD_DIR/main.txt" ]; then
  echo "No main.txt found in $PAYLOAD_DIR. Sending fallback notice."
  fallback="# Nightly Checking
通知ペイロードが見つかりませんでした。詳細は Actions を参照してください。
${ACTIONS_URL}"
  post_content "$fallback"
  exit 0
fi

echo "Posting main.txt ..."
post_content "$(cat "$PAYLOAD_DIR/main.txt")"

shopt -s nullglob
for f in "$PAYLOAD_DIR"/chunk_*.txt; do
  echo "Posting $(basename "$f") ..."
  post_content "$(cat "$f")"
  sleep 1
done

echo "Discord notification done."
