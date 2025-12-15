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

    // Phase 2B: エッジケース

    // T6: Data Class (ネスト 2階層)
    val level2Field = EdgeCaseFields.level2Parent(
        label = "parent",
        initialValue = Level2Parent(Level2Child("value")),
    )
    val level2ChildFactory = EdgeCaseFields.level2Parent.childField()

    // T7: Sealed Interface (data class サブクラス含む)
    val mixedSealedField = EdgeCaseFields.mixedSealed(
        label = "mixed",
        initialValue = MixedSealed.Loading,
        initialSuccess = MixedSealed.Success("data"),
    )
    val mixedLoadingFactory = EdgeCaseFields.mixedSealed.loadingField()
    val mixedSuccessFactory = EdgeCaseFields.mixedSealed.successField()

    // T8: Data Class (サポート外の型を含む) - unknownField は TODO() になるためカスタム実装が必要
    // val withUnsupportedField = EdgeCaseFields.withUnsupported(...)

    // T9: ネスト 3階層
    val level3Field = EdgeCaseFields.level3Root(
        label = "root",
        initialValue = Level3Root(Level3Middle(Level3Leaf(42))),
    )
    val level3L2Factory = EdgeCaseFields.level3Root.l2Field()

    // T10: Sealed の中に Sealed
    val outerSealedField = EdgeCaseFields.outerSealed(
        label = "outer",
        initialValue = OuterSealed.Idle,
        initialWithInner = OuterSealed.WithInner(InnerSealed.A),
    )
    val outerIdleFactory = EdgeCaseFields.outerSealed.idleField()
    val outerWithInnerFactory = EdgeCaseFields.outerSealed.withInnerField()

    // T11: 5階層ネスト
    val deep1Field = EdgeCaseFields.deep1(
        label = "deep",
        initialValue = Deep1(Deep2(Deep3(Deep4(Deep5("value"))))),
    )
    val deep1D2Factory = EdgeCaseFields.deep1.d2Field()

    // T12: internal visibility
    val internalField = InternalFields.simpleData(
        label = "internal",
        initialValue = SimpleData("", 0),
    )
    val internalNameFactory = InternalFields.simpleData.nameField()

    // T13: autoLabelByTypeName = true
    val autoLabelField = AutoLabelFields.simpleData(
        // label はデフォルト値 "simpleData" が使われる
        initialValue = SimpleData("", 0),
    )

    // T14: name カスタマイズ
    val customNameField = CustomNameFields.integer("label", 42)
}
