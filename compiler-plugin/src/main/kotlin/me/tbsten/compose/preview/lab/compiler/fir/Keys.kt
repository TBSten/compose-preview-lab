package me.tbsten.compose.preview.lab.compiler.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey

/**
 * Compose Preview Lab compiler plugin が synthesize する宣言を識別する key 群。
 *
 * Pattern adapted from Metro
 * (https://github.com/ZacSweers/metro/blob/main/compiler/src/main/kotlin/dev/zacsweers/metro/compiler/fir/Keys.kt).
 */
internal object Keys {
    /**
     * `@Preview` ごとに 1 つ生成する hint 関数を識別する key。
     *
     * 各 `@Preview` 関数に対し
     * `me.tbsten.compose.preview.lab.hints/previewHint(value: PreviewHintMarker_<hash>): CollectedPreview`
     * という hint stub を FIR で declare し、 IR で body を埋める。 fixed name + marker class
     * param で IdSignature を `@Preview` ごとに区別する。
     *
     * 使用箇所:
     * - [PreviewHintFirGenerator] (FIR side): hint stub に origin として attach
     * - [me.tbsten.compose.preview.lab.compiler.ir.PreviewHintIrBodyFiller] (IR side):
     *   この key で hint 関数を識別し、 body に `CollectedPreview(...)` constructor 呼び出しを
     *   `irReturn` する形で埋める
     */
    object PreviewLabHint : GeneratedDeclarationKey()

    /**
     * `@Preview` ごとに 1 つ生成する marker interface を識別する key。 hint 関数の引数型として
     * 使われ、 KLIB IdSignature を `@Preview` ごとにユニークにするだけが目的。 IC 衝突回避の
     * ため `interface` (`Modality.ABSTRACT`) で emit される。
     */
    object PreviewLabHintMarker : GeneratedDeclarationKey()
}
