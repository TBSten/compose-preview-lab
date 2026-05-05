package me.tbsten.compose.preview.lab

import androidx.compose.runtime.Composable

/**
 * Compose Preview Lab compiler plugin が emit する per-declaration hint 関数に attach される
 * metadata 運搬用 annotation。
 *
 * V2 hint 機構（Metro 風 per-declaration hint）では、 `@Preview` ごとに 1 つの hint 関数が
 * `me.tbsten.compose.preview.lab.exports` パッケージに `previewHint_<sha256(sourceFqn)>` という
 * 名前で生成され、 この annotation を介して `CollectedPreview` の全 metadata を carry する。
 *
 * 下流の `collectAllModulePreviews()` 呼び出しは、 KLIB / kotlin.Metadata から
 * `@PreviewHint` 付き関数を発見し、 ここに記録された各 field を `CollectedPreview` に復元する。
 *
 * ## Nullable の sentinel 表現
 *
 * Kotlin の annotation parameter は nullable を取れないため、 `CollectedPreview` の
 * nullable フィールド（`String?`, `Int?`） は **sentinel 値** で表現する：
 *
 * - `String?` の null → `""` (空文字列)
 * - `Int?` の null → `-1` (line 番号は 1-based なので衝突しない)
 *
 * producer (compiler plugin) は `PreviewFunctionInfo` の null を sentinel に変換、
 * consumer (`collectAllModulePreviews()`) は sentinel を null に戻して `CollectedPreview` を構築する。
 *
 * ## 利用者向け注意
 *
 * このアノテーションは **compiler plugin が emit するもの** であり、 user は直接書かない。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class PreviewHint(
    val id: String,
    val displayName: String,
    val filePath: String,
    val startLineNumber: Int,
    val endLineNumber: Int,
    val code: String,
    val kdoc: String,
)

/**
 * V2 hint 関数の戻り値型。 `@Composable () -> Unit` の typealias。
 *
 * compiler plugin が生成する hint 関数 (`previewHint_<hash>()`) はこの型を return する。
 * typealias 定義側に `@Composable` annotation を乗せることで、 利用側（FIR generator や
 * IR body filler、 consumer 側 discovery） は typealias を ClassId 参照するだけで `@Composable`
 * 処理が行われる。
 *
 * このアノテーションは **compiler plugin が emit する hint 関数の戻り値型**として使用される。
 * user は直接利用しない。
 */
public typealias PreviewContent = @Composable () -> Unit
