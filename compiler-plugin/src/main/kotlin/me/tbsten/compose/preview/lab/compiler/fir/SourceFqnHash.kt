package me.tbsten.compose.preview.lab.compiler.fir

import java.security.MessageDigest

/**
 * Per-declaration hint 関数名 suffix を `@Preview` の source FQN から導出する hash 関数。
 *
 * 「モジュール集約 marker」 を per-module で hash する [computeModuleHash] とは別物。
 * per-declaration 方式では「`@Preview` 1 個 = hint 1 個」 で入力は **sourceFqn のみ**。
 * projectRootPath / moduleName を入力に含めると同じ `@Preview` の hint 関数名が build 環境で
 * 変動し、 incremental compile / 再現可能ビルド両方を破壊するため含めない。
 *
 * 同 FQN cross-artifact collision (e.g. 別 artifact に `com.example.Foo` が同名で存在) は
 * **user 側の名前空間管理で解決する受容済み edge case** とする (`.local/redesign-hint-mechanism/参考.md`
 * §3 参照)。
 *
 * **Sample call**: `computeSourceFqnHash("uiLib.button.MyButton")`
 *
 * **Result**: `"a3k9z2x1"` 程度の base-36 8 文字 ([computeModuleHash] と同じ format)
 *
 * SHA-256 → 先頭 8 byte (64 bit) → base-36 で encode → 末尾 8 文字。 この格子は約 41 bit 強で、
 * 名前空間サイズに対する collision 確率は実用上十分小さい。
 */
internal fun computeSourceFqnHash(sourceFqn: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(sourceFqn.toByteArray(Charsets.UTF_8))
    val truncated = java.math.BigInteger(1, digest.copyOf(8))
    val encoded = truncated.toString(36)
    return encoded.takeLast(8).padStart(8, '0')
}
