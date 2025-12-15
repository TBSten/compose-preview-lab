@file:OptIn(ExperimentalComposePreviewLabApi::class)

package me.tbsten.compose.preview.lab.test.autogenerate

import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi

/**
 * このファイルは生成されたコードがコンパイル可能かどうかを確認するためのもの。
 * 実行時テストではなく、ビルドが成功すればテスト通過。
 */
@Suppress("UNUSED_VARIABLE", "unused")
fun verifyGeneratedFields() {
    // Phase 2A: 最小限のテスト

    // T1: プリミティブ型
    val stringField = Fields.string("label", "initial")
    val intField = Fields.int("label", 0)

    // T2: Enum
    val enumField = Fields.simpleStatus("label", SimpleStatus.LOADING)

    // T3: Object
    val objectField = Fields.singletonState("label")
    val dataObjectField = Fields.emptyState("label")

    // T4: Data Class
    val dataClassField = Fields.simpleData(
        label = "label",
        initialValue = SimpleData("", 0),
    )
    // ChildFieldFactories の確認
    val nameFieldFactory = Fields.simpleData.nameField()
    val countFieldFactory = Fields.simpleData.countField()

    // T5: Sealed Interface
    val sealedField = Fields.simpleSealed(
        label = "label",
        initialValue = SimpleSealed.A,
    )

    // Phase 2B: エッジケース（最初はコメントアウト）
    // ...
}
