package me.tbsten.compose.preview.lab.compiler.fir

import java.security.MessageDigest

/**
 * Per-declaration hint 関数名 suffix を `@Preview` の canonical signature key から導出する
 * hash 関数。
 *
 * 「モジュール集約 marker」 を per-module で hash する [computeModuleHash] とは別物。
 * per-declaration 方式では「`@Preview` 1 個 = hint 1 個」 で入力は **canonical key のみ**。
 * projectRootPath / moduleName を入力に含めると同じ `@Preview` の hint 関数名が build 環境で
 * 変動し、 incremental compile / 再現可能ビルド両方を破壊するため含めない。
 *
 * 同 canonical key cross-artifact collision (e.g. 別 artifact に同 FQN + 同 signature の
 * `@Preview` が存在) は **user 側の名前空間管理で解決する受容済み edge case** とする
 * (`.local/redesign-hint-mechanism/参考.md` §3 参照)。
 *
 * **Sample call**: `computeHintHash("uiLib.button.MyButton()")`
 *
 * **Result**: `"a3k9z2x1"` 程度の base-36 8 文字 ([computeModuleHash] と同じ format)
 *
 * SHA-256 → 先頭 8 byte (64 bit) → base-36 で encode → 末尾 8 文字。 この格子は約 41 bit 強で、
 * 名前空間サイズに対する collision 確率は実用上十分小さい。
 *
 * # Canonical key の format
 *
 * 同名 overload (例: 同 package に `fun MyButton()` と `fun MyButton(text: String)` があるケース)
 * を区別するため、 caller は **canonical key として `<sourceFqn>(<paramTypeFqns>)` 形式** を
 * 渡すこと:
 *
 * - sourceFqn: `<package>.<simpleName>`
 * - paramTypeFqns: 各パラメータの type FQN を `,` で連結。 nullability は `?` を suffix
 *   (例: `kotlin.String?`)
 *
 * [buildPreviewHintCanonicalKey] が FIR / IR それぞれの API から canonical key を構築する
 * helper。
 */
internal fun computeHintHash(canonicalKey: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(canonicalKey.toByteArray(Charsets.UTF_8))
    val truncated = java.math.BigInteger(1, digest.copyOf(8))
    val encoded = truncated.toString(36)
    return encoded.takeLast(8).padStart(8, '0')
}

/**
 * `<sourceFqn>(<paramTypeFqn1>,<paramTypeFqn2>,...)` という canonical key を組み立てる。
 * FIR / IR で同じ key を生成するために `parameterTypeFqns` の生成方法を caller 側で揃えること。
 *
 * **Sample**: `buildPreviewHintCanonicalKey("uiLib.MyButton", emptyList())` → `"uiLib.MyButton()"`
 * **Sample**: `buildPreviewHintCanonicalKey("uiLib.MyButton", listOf("kotlin.String"))` → `"uiLib.MyButton(kotlin.String)"`
 */
internal fun buildPreviewHintCanonicalKey(sourceFqn: String, parameterTypeFqns: List<String>): String =
    "$sourceFqn(${parameterTypeFqns.joinToString(",")})"

/**
 * Marker interface 短名 `PreviewHintMarker_<sanitized_fqn>_<hash>` を組み立てる。
 *
 * sanitized FQN は `[A-Za-z0-9_]` 以外をすべて `_` に置換した値で、 IDE / stack trace /
 * KLIB IC log で marker がどの `@Preview` 由来か一目で分かるようにするためのデバッグ補助。
 * 単純な `.` → `_` だと、 backtick で囲まれた識別子 (例: `` fun `my preview`() ``) のように
 * Kotlin source としては valid だが identifier として不正な文字を含む FQN が来た時に
 * `Name.identifier(...)` で例外を起こすため、 ここで包括的にサニタイズしておく。
 *
 * hash は同名 overload の区別用 ([buildPreviewHintCanonicalKey] の sha256) で、 sanitization
 * による情報落ち (`A.B` と `A_B` が衝突する等) も hash 側で吸収される。
 *
 * **Sample**: `buildMarkerShortName("uilib.button.MyButton", "a3k9z2x1")`
 * → `"PreviewHintMarker_uilib_button_MyButton_a3k9z2x1"`
 *
 * **Sample (識別子 不正文字を含むケース)**: `buildMarkerShortName("uilib.`my preview`", "h4sh1234")`
 * → `"PreviewHintMarker_uilib__my_preview__h4sh1234"`
 */
internal fun buildMarkerShortName(sourceFqn: String, hash: String): String {
    val sanitizedFqn = sourceFqn.replace(NonIdentifierCharRegex, "_")
    return "${PreviewLabFirBuiltIns.PreviewHintMarkerPrefix}${sanitizedFqn}_$hash"
}

/** Kotlin の identifier (back-tick なし版) として使えない文字。 */
private val NonIdentifierCharRegex = Regex("[^A-Za-z0-9_]")
