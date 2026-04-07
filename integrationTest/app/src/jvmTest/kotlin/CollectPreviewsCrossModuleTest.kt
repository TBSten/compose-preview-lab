package app

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan

/**
 * collectAllModulePreviews() のクロスモジュール集約テスト。
 *
 * JS/WasmJS では依存モジュールの @Composable ラムダ初期化で TypeError が発生するため
 * JVM でのみ実行する。
 */
class CollectPreviewsCrossModuleTest :
    FunSpec({
        test("collectAllModulePreviews は依存モジュールの Preview も含み collectModulePreviews より多い") {
            appPreviews.shouldNotBeEmpty()
            appPreviews.size shouldBeGreaterThan appModulePreviews.size
        }
    })
