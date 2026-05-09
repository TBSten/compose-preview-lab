# Failure classification heuristics

CI job が `conclusion == "FAILURE"` だったときの種別判定。 上から順に試して、 最初にマッチしたものを採用する。 transient infra を **最初** に置くのは、 その種別なら「コードに触らず rerun だけ」 で済むので、 誤ってコード側に分類して fix-ci-* に委ねないため。

## 1. transient infra failure

ジョブログに以下のいずれかのフレーズがあれば transient infra と判定:

- `api.foojay.io` (toolchain redirect で HTTP 4xx / 5xx)
- `Could not GET 'https://...'` / `Could not HEAD 'https://...'`
- `Received status code 5\d\d` / `Received status code 502 / 503 / 504`
- `gradle-build-action` 系の `Failed to download` / `connection reset`
- `actions/setup-java` の cache restore 失敗 (`Failed to restore`、 ただし build に進んで他の error が出ているなら別種別)
- macOS / Windows runner 起動失敗 (`The runner has received a shutdown signal`)
- `npm ERR! network` / `EAI_AGAIN` / `ECONNRESET`
- Docker pull 失敗 (`Error response from daemon: pull access denied` でない方)

判定後の対応: `gh run rerun <runId> --failed` を打つ。 失敗 (in-progress) なら **次回ループに送る**。

## 2. lint

ジョブ名や log の特徴:

- ジョブ名に `Lint` / `ktlint` / `eslint` / `Format` を含む
- log に `ktlintCheck FAILED` / `Lint task FAILED`
- log に `standard:` から始まる ktlint 違反行
- `eslint --fix` で消える系の error pattern

委譲先: **fix-ci-lint** skill (project に存在する場合)。

## 3. binary compat

ジョブ名や log の特徴:

- ジョブ名に `Validate Binary Compatibility` / `apiCheck` / `BCV`
- log に `Task :*:apiCheck FAILED`
- log に `API check failed for project` 直後の `--- ... +++ ... @@` diff

委譲先: **fix-ci-binary** skill。 委譲先が apiDump で再生成する手順を知っていることを前提とする。

注意: integrationTest など **composite build** がある場合、 親の apiDump と integrationTest 側の apiDump が **別々** に必要なケースあり。 fix-ci-binary がそのへんを面倒見るか、 このスキルから別途指示する。

## 4. test

ジョブ名や log の特徴:

- ジョブ名に `Test` / `jvmTest` / `jsBrowserTest` / `wasmJsBrowserTest` / `iosSimulatorArm64Test` / `testDebugUnitTest`
- log に `> Task :*:test FAILED`
- log に `org.opentest4j.AssertionFailedError` / `java.lang.AssertionError` / Kotest の `MultiAssertionError`

委譲先: **fix-ci-test** skill。

注意: `Test JS` や `Test iOS` は環境依存で **同じコードが local では pass、 CI で fail** することがある。 そのケースは fix-ci-test 内で「local 再現」 を試みる。 再現しないなら transient 寄り (= rerun 候補) として扱うことも検討。

## 5. build

ジョブ名や log の特徴:

- ジョブ名に `Publish to Maven Local` / `Build` / `Compile` / `assembleDebug` / `assembleRelease`
- log に `> Task :*:compileKotlin* FAILED` / `Could not resolve all dependencies`
- log に `Unresolved reference` / `Type mismatch` (Kotlin compile error)

委譲先: **fix-ci-build** skill。 build error は lint / test / binary compat と隣接領域で、 root cause 次第では別 skill が引き継ぐべきこともある。 fix-ci-build 内で適切に re-classify してもらう前提でよい。

## 6. unknown

上のどれにもマッチしない場合は、 unknown として **ユーザーに報告のみ** で勝手に直そうとしない。 log の末尾 50 行と job URL を提示する。

## ヒューリスティクスの注意

これは雑なパターンマッチで、 false-positive はある。 例えば:

- `Test JS` ジョブの中で「`KTOR-8464`」 という warning が出る → これは warn であって fail ではない、 同 job の真の failure 行を別に探す必要
- `binary compatibility` というフレーズを含む test (apiCheck そのものではない) → ジョブ名で binary compat と test を混同しないよう、 task 名 (`*:apiCheck` / `*:test`) を併用判定

迷ったら **log の末尾の `> Task :*: FAILED` 行を必ず確認** し、 task 名で再判定する。 ジョブ名は表示用、 task 名が真実。
