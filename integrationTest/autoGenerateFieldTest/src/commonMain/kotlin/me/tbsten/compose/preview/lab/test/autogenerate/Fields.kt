package me.tbsten.compose.preview.lab.test.autogenerate

import me.tbsten.compose.preview.lab.AutoGenerateField
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi

// Phase 2A: 最小限のテスト
@OptIn(ExperimentalComposePreviewLabApi::class)
@AutoGenerateField<String>
@AutoGenerateField<Int>
@AutoGenerateField<SimpleStatus>
@AutoGenerateField<SingletonState>
@AutoGenerateField<EmptyState>
@AutoGenerateField<SimpleData>
@AutoGenerateField<SimpleSealed>
object Fields

// Phase 2B: エッジケース
@OptIn(ExperimentalComposePreviewLabApi::class)
@AutoGenerateField<Level2Parent>
@AutoGenerateField<MixedSealed>
@AutoGenerateField<WithUnsupported>
@AutoGenerateField<Level3Root>
@AutoGenerateField<OuterSealed>
@AutoGenerateField<Deep1>
object EdgeCaseFields

// T12: internal visibility 用
@OptIn(ExperimentalComposePreviewLabApi::class)
@AutoGenerateField<SimpleData>
internal object InternalFields

// T13: autoLabelByTypeName = true 用
@OptIn(ExperimentalComposePreviewLabApi::class)
@AutoGenerateField<SimpleData>(autoLabelByTypeName = true)
object AutoLabelFields

// T14: name カスタマイズ用
@OptIn(ExperimentalComposePreviewLabApi::class)
@AutoGenerateField<Int>(name = "integer")
object CustomNameFields
