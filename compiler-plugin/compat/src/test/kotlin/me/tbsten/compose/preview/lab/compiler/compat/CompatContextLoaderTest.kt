package me.tbsten.compose.preview.lab.compiler.compat

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Loader 単体テスト。実 ServiceLoader を使うのではなく、resolve ロジック自体は
 * Phase 4 以降に compat module が揃うと統合テストできるため、ここでは
 * `CompatContext.load(knownVersion)` 経由で実 classpath ベースの動作だけ確認する。
 *
 * Phase 1 時点では classpath に Factory 実装が無いので "No factories" エラーが出ることを確認。
 */
class CompatContextLoaderTest :
    StringSpec({
        "factory が無いと error" {
            val ex = shouldThrow<IllegalStateException> {
                CompatContext.load(KotlinToolingVersion("2.3.21"))
            }
            ex.message?.contains("No CompatContext.Factory") shouldBe true
        }
    })
