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
//   DISCORD_THREAD_ID       : (任意) 既存 thread の ID。 指定があればその thread に投稿する
//   DISCORD_THREAD_NAME     : (任意) thread の名前。 デフォルトは "Nightly Checking YYYY-MM-DD"
//
// 動作:
//   1. issue Markdown ファイルからタイトル / メタ行 / 詳細を抽出
//   2. **1 通目**: チャンネル本体に **短いサマリ** (PBT セクション + 探索的テストヘッダー)
//      を送る。 詳細 issue は含めない。 探索的テスト issue があるときは thread を作る or
//      既存 thread を使う。
//   3. **2 通目以降**: 詳細 issue の `## 探索的テスト N. ...` を thread に流す。
//      thread が確保できなかった場合は最終手段としてチャンネルに連投する。
//
// thread の確保方法:
//   - `DISCORD_THREAD_ID` があれば既存 thread に投稿
//   - 無ければ `?thread_name=` を 1 通目に付けて新規 thread を作る (forum/media channel 前提)
//     → 1 通目のレスポンスから channel_id (= 新しく作られた thread の ID) を拾って続きで使う
//   - どちらも駄目だった場合のみ、 詳細をチャンネルに連投するフォールバック

const fs = require('fs');
const path = require('path');

const CHUNK_LIMIT = 1900;
const CONTENT_LIMIT = 1990;
const THREAD_NAME_LIMIT = 100;

// 重要度の星表記。 P0 が最重要 (4 個満点) で、 番号が増えるほど星が減る。
// issue Markdown 本体は `P0`〜`P3` のまま保持し、 Discord 通知でだけ星に変換する。
const SEVERITY_STARS = {
    P0: '⭐⭐⭐⭐',
    P1: '⭐⭐⭐☆',
    P2: '⭐⭐☆☆',
    P3: '⭐☆☆☆',
};

// 見出しに付ける絵文字。 被らないように 3 種類を割り当てる。
const EMOJI = {
    pbt: '🧪', // Property-Based Test → 試験管
    exploration: '🔍', // 探索的テスト → 虫眼鏡
    issue: '📌', // 個別 issue → ピン留め
};

function decorateSeverity(line) {
    // 例: `**重要度**: P1` → `**重要度**: ⭐⭐⭐☆ (P1)`
    //     `**重要度**: P1 補足` → `**重要度**: ⭐⭐⭐☆ (P1) 補足`
    const m = line.match(/^(\s*\*\*重要度\*\*:\s*)(P[0-3])\b(.*)$/);
    if (!m) return line;
    const stars = SEVERITY_STARS[m[2]];
    if (!stars) return line;
    return `${m[1]}${stars} (${m[2]})${m[3]}`;
}

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
    const sevDecorated = sevMatch ? decorateSeverity(sevMatch[0]) : null;
    let meta = '';
    if (catMatch && sevDecorated) {
        meta = `${catMatch[0]} / ${sevDecorated}`;
    } else if (catMatch) {
        meta = catMatch[0];
    } else if (sevDecorated) {
        meta = sevDecorated;
    }

    // `## 詳細` セクションを抜き出す。 JavaScript の RegExp は `\Z` を理解しない (リテラル
     // 扱いになる) ので、 「ヘッダの位置を探す → ヘッダ次行から、 次の `##` までを切る」
     // という 2 段階で処理する。
    let detail = '';
    const detailHeaderMatch = text.match(/^## 詳細[ \t]*$/m);
    if (detailHeaderMatch) {
        const afterHeader = text
            .slice(detailHeaderMatch.index + detailHeaderMatch[0].length)
            .replace(/^\r?\n/, '');
        const nextHeader = afterHeader.search(/^## /m);
        const body = nextHeader >= 0 ? afterHeader.slice(0, nextHeader) : afterHeader;
        detail = body
            .split(/\r?\n/)
            .map((l) => l.trim())
            .filter((l) => l.length > 0)
            .slice(0, 3)
            .join(' ')
            .slice(0, 200);
    }

    return { title, meta, detail };
}

function readIssues(issuesDir) {
    if (!fs.existsSync(issuesDir)) return [];
    return fs
        .readdirSync(issuesDir)
        .filter((f) => f.endsWith('.md'))
        .sort()
        .map((f) => extractIssue(path.join(issuesDir, f)));
}

function buildSummary(env, issues) {
    // 1 通目: チャンネル本体に出すサマリ。 詳細 issue は含めない。
    const lines = [];

    if (env.SEND_PBT === 'true') {
        lines.push(`# ${EMOJI.pbt} PBT`);
        lines.push(`ステータス: ${env.PBT_RESULT || '(unknown)'}`);
        if (env.ACTIONS_URL) lines.push(`Actions: ${env.ACTIONS_URL}`);
    }

    if (env.SEND_EXP === 'true') {
        if (lines.length > 0) lines.push('');
        lines.push(`# ${EMOJI.exploration} 探索的テスト`);
        if (env.EXP_RESULT === 'failure' || env.EXP_RESULT === 'cancelled') {
            lines.push('(探索ジョブが失敗または途中終了しています。 部分結果を表示します)');
        }
        lines.push(`発見件数: ${env.EXP_COUNT || '0'}件`);
        if (env.ACTIONS_URL) lines.push(`Actions: ${env.ACTIONS_URL}`);
        // 「詳細は thread を参照」 の案内は ルーティング結果が確定した後で
        // 呼び出し側 (notifyDiscord) が付与する。 ここでは付けない。
    }

    return lines.join('\n');
}

function buildDetailText(issues) {
    // 2 通目以降: 各 issue を `## 探索的テスト N. <title>` 形式で並べる。
    if (issues.length === 0) return '';
    const blocks = issues.map((issue, i) => {
        const block = [`## ${EMOJI.issue} 探索的テスト ${i + 1}. ${issue.title}`];
        if (issue.meta) block.push(issue.meta);
        if (issue.detail) block.push(issue.detail);
        return block.join('\n');
    });
    return blocks.join('\n\n');
}

function splitIntoChunks(fullText, chunkLimit) {
    if (!fullText) return [];
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
    if (current.length > 0) chunks.push(current);
    return chunks;
}

function todayYmd() {
    return new Date().toISOString().slice(0, 10);
}

function buildWebhookUrl(webhook, { threadId, threadName, wait } = {}) {
    const url = new URL(webhook);
    if (wait) url.searchParams.set('wait', 'true');
    if (threadId) url.searchParams.set('thread_id', threadId);
    if (threadName) url.searchParams.set('thread_name', threadName.slice(0, THREAD_NAME_LIMIT));
    return url.toString();
}

function truncateContent(content) {
    if (content.length <= CONTENT_LIMIT) return content;
    return content.slice(0, CONTENT_LIMIT - 5) + '...';
}

async function postMessage(webhook, content, opts, core) {
    const url = buildWebhookUrl(webhook, opts);
    const body = truncateContent(content);
    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ content: body, allowed_mentions: { parse: [] } }),
    });
    if (!res.ok) {
        const text = await res.text().catch(() => '');
        throw new Error(`Discord POST failed: HTTP ${res.status} ${text}`);
    }
    core.info(`Posted ${body.length} chars${opts.threadId ? ` (thread=${opts.threadId})` : opts.threadName ? ` (new thread="${opts.threadName}")` : ''}`);
    if (opts.wait) {
        const json = await res.json().catch(() => null);
        return json;
    }
    return null;
}

module.exports = async function notifyDiscord({ core, env = process.env }) {
    const webhook = env.DISCORD_WEBHOOK_URL;
    if (!webhook) {
        core.info('DISCORD_WEBHOOK_URL is empty; skipping Discord notification.');
        return;
    }

    const sendPbt = env.SEND_PBT === 'true';
    const sendExp = env.SEND_EXP === 'true';
    if (!sendPbt && !sendExp) {
        core.info(`No content to send (SEND_PBT=${env.SEND_PBT}, SEND_EXP=${env.SEND_EXP}). Skipping.`);
        return;
    }

    const issues = sendExp ? readIssues(env.ISSUES_DIR || '.local/nightly-exploration/issues') : [];
    const summary = buildSummary(env, issues);
    const detailText = buildDetailText(issues);

    const presetThreadId = (env.DISCORD_THREAD_ID || '').trim() || null;
    const threadName =
        (env.DISCORD_THREAD_NAME || '').trim() || `Nightly Checking ${todayYmd()}`;

    const wantsThread = detailText.length > 0;

    // 1 通目のルーティングと文言を、 thread が確保できる経路かどうかで出し分ける。
    // - DISCORD_THREAD_ID 指定あり: summary も詳細も同じ thread に投稿。 案内文不要
    //   (summary 自体が thread 内にあるので "詳細は thread を参照" は意味をなさない)。
    // - 指定なし & 詳細あり: 1 通目に `thread_name` を付けて新規 thread 作成を狙う。
    //   1 通目はチャンネル本体に残るため、 「詳細は thread を参照」 の案内文を付ける。
    // - 指定なし & 詳細なし: チャンネルに summary だけ。 案内文なし。
    // - 上の thread_name 投稿が HTTP エラーになった場合: 案内文を外した summary を
    //   channel に再送し、 詳細もチャンネル連投にフォールバック。
    let resolvedThreadId = presetThreadId;
    const summaryWithNote = `${summary}\n詳細は thread を参照してください。`;

    let summaryToPost;
    let summaryOpts;
    if (presetThreadId) {
        summaryToPost = summary;
        summaryOpts = { threadId: presetThreadId, wait: false };
    } else if (wantsThread) {
        summaryToPost = summaryWithNote;
        summaryOpts = { threadName, wait: true };
    } else {
        summaryToPost = summary;
        summaryOpts = { wait: false };
    }

    try {
        const firstResponse = await postMessage(webhook, summaryToPost, summaryOpts, core);
        if (!resolvedThreadId && firstResponse && firstResponse.channel_id) {
            // forum/media channel で thread_name を指定すると、 レスポンスの channel_id が
            // 新たに作られた thread の ID になる。
            resolvedThreadId = firstResponse.channel_id;
        }
    } catch (err) {
        if (summaryOpts.threadName) {
            // 通常のテキストチャンネル等で thread_name が拒否されるケース。
            // 案内文を外した summary を channel に再送し、 詳細はチャンネル連投にする。
            core.warning(
                `1 通目が thread_name 付きで失敗 (${err.message})。 案内文を外して channel 連投にフォールバックします。`,
            );
            await postMessage(webhook, summary, { wait: false }, core);
            resolvedThreadId = null;
        } else {
            throw err;
        }
    }

    if (!wantsThread) {
        core.info('Discord notification done (summary only).');
        return;
    }

    // 2 通目以降: 詳細を thread に流す。 thread 確保できなかった場合はチャンネル連投。
    const detailChunks = splitIntoChunks(detailText, CHUNK_LIMIT);
    core.info(`Detail split into ${detailChunks.length} chunk(s).`);

    for (let i = 0; i < detailChunks.length; i++) {
        const opts = resolvedThreadId ? { threadId: resolvedThreadId } : {};
        await postMessage(webhook, detailChunks[i], opts, core);
        if (i + 1 < detailChunks.length) {
            await new Promise((r) => setTimeout(r, 1000));
        }
    }

    core.info('Discord notification done.');
};

module.exports._internal = {
    extractIssue,
    readIssues,
    buildSummary,
    buildDetailText,
    splitIntoChunks,
    buildWebhookUrl,
};
