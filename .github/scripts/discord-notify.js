// Nightly Checking ワークフロー用 Discord 通知スクリプト。
// `actions/github-script@v7` から `require(...)` で読み込み、 渡された `core` 経由でログを出す。
//
// 環境変数 (workflow の env でセットする):
//   SEND_PBT  / SEND_EXP    : "true" のときに各セクションを含める
//   PBT_RESULT / EXP_RESULT : needs.<job>.result ("success" | "failure" | "cancelled" | "skipped")
//   EXP_COUNT               : exploratory ジョブが報告した issue 件数
//   ACTIONS_URL             : Actions Run の URL
//   ISSUES_DIR              : .local/nightly-exploration/issues
//   DISCORD_WEBHOOK_URL     : Discord webhook URL (必須)
//
// 動作:
//   1. issue Markdown ファイルからタイトル / メタ行 / 詳細を抽出
//   2. PBT セクションと探索的テストセクションを条件付きで組み立て
//   3. Discord の 2000 文字制限を超えそうなら行単位で 1900 文字ごとに分割し、
//      同じチャンネルへ順次連投 (webhook で thread を作るのはチャンネル種別依存のため、
//      連投で代替する)。

const fs = require('fs');
const path = require('path');

const CHUNK_LIMIT = 1900;
const CONTENT_LIMIT = 1990;

function extractIssue(filePath) {
    const text = fs.readFileSync(filePath, 'utf8');

    let title = '(タイトル未設定)';
    for (const line of text.split(/\r?\n/)) {
        const m = line.match(/^#\s+(.+?)\s*$/);
        if (m) {
            title = m[1];
            break;
        }
    }

    const catMatch = text.match(/^\*\*カテゴリ\*\*:.+$/m);
    const sevMatch = text.match(/^\*\*重要度\*\*:.+$/m);
    let meta = '';
    if (catMatch && sevMatch) {
        meta = `${catMatch[0]} / ${sevMatch[0]}`;
    } else if (catMatch) {
        meta = catMatch[0];
    } else if (sevMatch) {
        meta = sevMatch[0];
    }

    let detail = '';
    const detailMatch = text.match(/^## 詳細\s*\r?\n([\s\S]*?)(?=^## |\Z)/m);
    if (detailMatch) {
        detail = detailMatch[1]
            .split(/\r?\n/)
            .map((l) => l.trim())
            .filter((l) => l.length > 0)
            .slice(0, 3)
            .join(' ')
            .slice(0, 200);
    }

    return { title, meta, detail };
}

function buildSections(env) {
    const sections = [];

    if (env.SEND_PBT === 'true') {
        const lines = ['# PBT', `ステータス: ${env.PBT_RESULT || '(unknown)'}`];
        if (env.ACTIONS_URL) {
            lines.push(`Actions: ${env.ACTIONS_URL}`);
        }
        sections.push(lines.join('\n'));
    }

    if (env.SEND_EXP === 'true') {
        const lines = ['# 探索的テスト'];
        if (env.EXP_RESULT === 'failure' || env.EXP_RESULT === 'cancelled') {
            lines.push('(探索ジョブが失敗または途中終了しています。部分結果を表示します)');
        }
        lines.push(`発見件数: ${env.EXP_COUNT || '0'}件`);
        if (env.ACTIONS_URL) {
            lines.push(`Actions: ${env.ACTIONS_URL}`);
        }

        const issuesDir = env.ISSUES_DIR || '.local/nightly-exploration/issues';
        if (fs.existsSync(issuesDir)) {
            const files = fs
                .readdirSync(issuesDir)
                .filter((f) => f.endsWith('.md'))
                .sort();
            files.forEach((f, i) => {
                const { title, meta, detail } = extractIssue(path.join(issuesDir, f));
                lines.push('', `## 探索的テスト ${i + 1}. ${title}`);
                if (meta) lines.push(meta);
                if (detail) lines.push(detail);
            });
        }

        sections.push(lines.join('\n'));
    }

    return sections;
}

function splitIntoChunks(fullText, chunkLimit) {
    const lines = fullText.split('\n');
    const chunks = [];
    let current = '';
    for (const line of lines) {
        const candidate = current.length === 0 ? line : `${current}\n${line}`;
        if (candidate.length > chunkLimit && current.length > 0) {
            chunks.push(current);
            current = line;
        } else {
            current = candidate;
        }
    }
    if (current.length > 0) {
        chunks.push(current);
    }
    return chunks;
}

async function postChunk(webhook, content, idx, core) {
    let body = content;
    if (body.length > CONTENT_LIMIT) {
        body = body.slice(0, CONTENT_LIMIT - 5) + '...';
    }
    const res = await fetch(webhook, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: body, allowed_mentions: { parse: [] } }),
    });
    if (!res.ok) {
        const text = await res.text();
        throw new Error(`Discord POST chunk #${idx} failed: HTTP ${res.status} ${text}`);
    }
    core.info(`Posted chunk #${idx} (${body.length} chars)`);
}

async function sendFallback(webhook, actionsUrl, core) {
    const content = [
        '# Nightly Checking',
        '通知ペイロードを生成できませんでした。詳細は Actions を参照してください。',
        actionsUrl,
    ]
        .filter(Boolean)
        .join('\n');
    await postChunk(webhook, content, 0, core);
}

module.exports = async function notifyDiscord({ core, env = process.env }) {
    const webhook = env.DISCORD_WEBHOOK_URL;
    if (!webhook) {
        core.info('DISCORD_WEBHOOK_URL is empty; skipping Discord notification.');
        return;
    }

    const sections = buildSections(env);
    if (sections.length === 0) {
        core.info(
            `No content to send (SEND_PBT=${env.SEND_PBT}, SEND_EXP=${env.SEND_EXP}). Skipping.`,
        );
        return;
    }

    const fullText = sections.join('\n\n');
    const chunks = splitIntoChunks(fullText, CHUNK_LIMIT);

    if (chunks.length === 0) {
        await sendFallback(webhook, env.ACTIONS_URL, core);
        return;
    }

    core.info(`Prepared ${chunks.length} chunk(s) for Discord.`);
    for (let i = 0; i < chunks.length; i++) {
        await postChunk(webhook, chunks[i], i + 1, core);
        if (i + 1 < chunks.length) {
            await new Promise((r) => setTimeout(r, 1000));
        }
    }
    core.info('Discord notification done.');
};

module.exports._internal = { extractIssue, buildSections, splitIntoChunks };
