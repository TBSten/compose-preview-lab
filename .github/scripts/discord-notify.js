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
//   DISCORD_THREAD_NAME     : (任意) thread の名前。 デフォルトは "🔍 探索的テスト: 発見件数 N件"
//
// 動作:
//   1. issue Markdown ファイルからタイトル / メタ行 / 詳細を抽出
//   2. **1 通目**: チャンネル本体に **短いサマリ** (PBT セクション + 探索的テストヘッダー)
//      を送る。 詳細 issue は含めない。 探索的テスト issue があるときは thread を作る or
//      既存 thread を使う。 forum/media channel ではこの 1 通目が thread の OP になる。
//   3. **2 通目以降**: 各 issue を **1 件 1 message** として thread に投稿する
//      (`## 探索的テスト N. ...`)。 thread が確保できなかった場合は最終手段として
//      チャンネルに連投する。
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

// `**重要度**: P1` のような行から `⭐⭐⭐☆ P1` のような短縮ラベルを取り出す。
// <details><summary> の中など 1 行で表示する場面用 (Markdown bold は描画されないので
// `**` を含めない)。
function severityToShortLabel(sevLine) {
    if (!sevLine) return null;
    const m = sevLine.match(/P[0-3]/);
    if (!m) return null;
    const stars = SEVERITY_STARS[m[0]];
    return stars ? `${stars} ${m[0]}` : m[0];
}

// `**カテゴリ**: cat2 (CI ログ・apiDump)` のような行から `cat2` だけを取り出す。
function categoryToShortLabel(catLine) {
    if (!catLine) return null;
    const m = catLine.match(/cat\d+/);
    return m ? m[0] : null;
}

function extractIssue(filePath) {
    const text = fs.readFileSync(filePath, 'utf8');
    const raw = text;

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

    // job summary の <details><summary> ラベル用に、 タイトル以外の情報を短縮形で取り出す。
    const severityShort = severityToShortLabel(sevMatch ? sevMatch[0] : null);
    const categoryShort = categoryToShortLabel(catMatch ? catMatch[0] : null);

    return { title, meta, detail, raw, severityShort, categoryShort };
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

function buildDetailMessages(issues) {
    // 1 issue = 1 message として配列で返す (thread 内で各発見項目が独立した投稿として見える)。
    // 単一 issue が CHUNK_LIMIT を超える稀ケースのみ、 行境界で更に分割する。
    const messages = [];
    issues.forEach((issue, i) => {
        const block = [`## ${EMOJI.issue} 探索的テスト ${i + 1}. ${issue.title}`];
        if (issue.meta) block.push(issue.meta);
        if (issue.detail) block.push(issue.detail);
        const content = block.join('\n');
        if (content.length <= CHUNK_LIMIT) {
            messages.push(content);
        } else {
            messages.push(...splitIntoChunks(content, CHUNK_LIMIT));
        }
    });
    return messages;
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

function buildWebhookUrl(webhook, { threadId, wait } = {}) {
    // Discord 仕様: `thread_id` は URL クエリで渡すが、 `thread_name` は **JSON body** に
    // 入れる必要がある。 URL クエリに thread_name を付けても無視され、 forum channel への
    // 投稿は 400 ("must have a thread_name or thread_id") になる。
    // そのため thread_name は呼び出し側で body に積み、 この関数は thread_id と wait のみ扱う。
    const url = new URL(webhook);
    if (wait) url.searchParams.set('wait', 'true');
    if (threadId) url.searchParams.set('thread_id', threadId);
    return url.toString();
}

function truncateContent(content) {
    if (content.length <= CONTENT_LIMIT) return content;
    return content.slice(0, CONTENT_LIMIT - 5) + '...';
}

async function postMessage(webhook, content, opts, core) {
    const url = buildWebhookUrl(webhook, { threadId: opts.threadId, wait: opts.wait });
    const body = truncateContent(content);
    const payload = { content: body, allowed_mentions: { parse: [] } };
    if (opts.threadName) {
        // `thread_name` は URL クエリではなく body に入れる (Discord 仕様)。
        payload.thread_name = opts.threadName.slice(0, THREAD_NAME_LIMIT);
    }
    const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
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

// Webhook が紐付いている channel の ID を取得する。 通常のテキストチャンネル webhook に
// `?thread_name=` を渡すと thread は作成されず channel 本体に投稿されるが、 レスポンスの
// `channel_id` には webhook 自身の channel ID が入ってしまう。 これを「新規 thread の ID」 と
// 誤認しないよう、 事前にこの値を取得して照合に使う。
async function getWebhookChannelId(webhook) {
    try {
        const res = await fetch(webhook);
        if (!res.ok) return null;
        const json = await res.json();
        return json && json.channel_id ? json.channel_id : null;
    } catch {
        return null;
    }
}

// GitHub Actions の job summary に PBT と 探索的テストの結果を書き出す。
// 各 issue は <details> で畳んで、 一覧の見通しを保つ。
async function writeJobSummary({ core, env, issues }) {
    if (!core || !core.summary || typeof core.summary.addHeading !== 'function') {
        return;
    }
    const sendPbt = env.SEND_PBT === 'true';
    const sendExp = env.SEND_EXP === 'true';
    if (!sendPbt && !sendExp) return;

    core.summary.addHeading('Nightly Checking 結果', 1);
    if (env.ACTIONS_URL) {
        core.summary.addRaw(`[Actions Run](${env.ACTIONS_URL})\n\n`);
    }

    if (sendPbt) {
        core.summary.addHeading('🧪 PBT', 2);
        core.summary.addRaw(`ステータス: \`${env.PBT_RESULT || '(unknown)'}\`\n\n`);
    }

    if (sendExp) {
        core.summary.addHeading('🔍 探索的テスト', 2);
        if (env.EXP_RESULT === 'failure' || env.EXP_RESULT === 'cancelled') {
            core.summary.addRaw(
                '> 探索ジョブが失敗または途中終了しています。 部分結果を表示します。\n\n',
            );
        }
        core.summary.addRaw(`発見件数: **${env.EXP_COUNT || issues.length}件**\n\n`);

        issues.forEach((issue, i) => {
            // <details><summary>...</summary> ラベルには 重要度 / カテゴリ / タイトル を並べ、
            // 折りたたんだ状態でも「何がどれくらい重要か」 一目で分かるようにする。
            // <summary> 内では Markdown bold が描画されないので `**` は使わず、 `|` 区切りで並べる。
            const labelParts = [];
            if (issue.severityShort) labelParts.push(issue.severityShort);
            if (issue.categoryShort) labelParts.push(issue.categoryShort);
            labelParts.push(issue.title);
            const summaryLabel = `📌 ${labelParts.join(' | ')}`;
            const bodyWithoutTitle = issue.raw.replace(/^#\s+.+?\r?\n+/, '').trimEnd();
            core.summary.addDetails(summaryLabel, `\n\n${bodyWithoutTitle}\n`);
        });
    }

    try {
        await core.summary.write();
    } catch (err) {
        // job summary 書き出しはベストエフォート。 失敗しても通知本体には影響させない。
        core.warning?.(`Job summary の書き込みに失敗: ${err.message}`);
    }
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
    const detailMessages = buildDetailMessages(issues);

    // Discord 通知の前に job summary を書き出しておく。 Discord 投稿が失敗しても
    // 結果は Actions UI 上で確認できる。
    await writeJobSummary({ core, env, issues });

    const presetThreadId = (env.DISCORD_THREAD_ID || '').trim() || null;
    // thread 名は探索的テストの発見件数を主題にする (forum channel で run ごとに作られる
    // thread タイトルに、 一目で件数が分かる形で表示するため)。 issues.length は wantsThread
    // 経路に入る前提なので 1 以上。 `DISCORD_THREAD_NAME` で上書きも可能。
    const threadName =
        (env.DISCORD_THREAD_NAME || '').trim() ||
        `🔍 探索的テスト: 発見件数 ${issues.length}件`;

    const wantsThread = detailMessages.length > 0;

    // 1 通目のルーティング。 summary はそのまま実行概要として送り出す:
    // - forum/media channel + thread_name → 1 通目が新規 thread の OP として残り、 件数を含む
    //   thread タイトルで一覧表示される
    // - DISCORD_THREAD_ID 指定あり → その既存 thread の中に投稿される
    // - text channel など (thread_name が無視されるケース) → channel 本体に投稿され、
    //   詳細も channel に連投される (forum channel への移行を推奨)
    let resolvedThreadId = presetThreadId;
    let summaryOpts;
    if (presetThreadId) {
        summaryOpts = { threadId: presetThreadId, wait: false };
    } else if (wantsThread) {
        summaryOpts = { threadName, wait: true };
    } else {
        summaryOpts = { wait: false };
    }

    // thread_name 経由で投稿する前に webhook の所属 channel_id を取得しておく
    // (text channel webhook では thread_name が無視され channel に直接投稿されるが、
    //  レスポンスの channel_id は webhook 自身の channel ID なので thread_id として
    //  使い回せない。 事前に照合できる値を握っておく)。
    const webhookChannelId = summaryOpts.threadName
        ? await getWebhookChannelId(webhook)
        : null;

    try {
        const firstResponse = await postMessage(webhook, summary, summaryOpts, core);
        if (!resolvedThreadId && summaryOpts.threadName && firstResponse && firstResponse.channel_id) {
            if (!webhookChannelId || firstResponse.channel_id !== webhookChannelId) {
                // forum/media channel で thread_name 指定 → 新規 thread が作られた。
                resolvedThreadId = firstResponse.channel_id;
            } else {
                // text channel で thread_name が無視され、 channel に直接投稿されたケース。
                // summary には案内文を含めていないため edit は不要、 詳細は channel 連投で出す。
                core.warning(
                    `thread_name 付き投稿は受理されたが thread は作成されなかった ` +
                    `(レスポンス channel_id=${firstResponse.channel_id} が webhook の channel と一致)。 ` +
                    `詳細は channel 連投にフォールバックします (forum channel への切り替えを推奨)。`,
                );
                resolvedThreadId = null;
            }
        }
    } catch (err) {
        if (summaryOpts.threadName) {
            // thread_name 付き投稿が HTTP エラーになる稀なケース。 channel 連投にフォールバック。
            core.warning(
                `1 通目が thread_name 付きで失敗 (${err.message})。 channel 連投にフォールバックします。`,
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

    // 2 通目以降: 1 issue = 1 message として thread に流す
    // (thread 確保できなかった場合は channel 連投)。
    core.info(`Posting ${detailMessages.length} detail message(s).`);

    for (let i = 0; i < detailMessages.length; i++) {
        const opts = resolvedThreadId ? { threadId: resolvedThreadId } : {};
        await postMessage(webhook, detailMessages[i], opts, core);
        if (i + 1 < detailMessages.length) {
            // Discord webhook rate limit: 30 req / min / channel. 500ms 間隔 = 120 req/min 相当
            // だが thread への投稿は緩めなので 500ms で十分安全。 ただし 30 件超なら 1s に。
            const delay = detailMessages.length > 30 ? 1000 : 500;
            await new Promise((r) => setTimeout(r, delay));
        }
    }

    core.info('Discord notification done.');
};

module.exports._internal = {
    extractIssue,
    readIssues,
    buildSummary,
    buildDetailMessages,
    splitIntoChunks,
    buildWebhookUrl,
    writeJobSummary,
};
