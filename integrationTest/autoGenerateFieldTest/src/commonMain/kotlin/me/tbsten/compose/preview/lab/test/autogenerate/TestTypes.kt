package me.tbsten.compose.preview.lab.test.autogenerate

// ========================================
// Phase 2A: 基本型テスト (最小限のテストケース)
// ========================================

// T1: プリミティブ型
// → Fields.string(), Fields.int() が生成されることを確認

// T2: Enum
enum class SimpleStatus { LOADING, SUCCESS, ERROR }
// → Fields.simpleStatus() が EnumField を返すことを確認

// T3: Object
object SingletonState
data object EmptyState
// → Fields.singletonState(), Fields.emptyState() が FixedField を返すことを確認

// T4: Data Class (プリミティブのみ)
data class SimpleData(val name: String, val count: Int)
// → Fields.simpleData() が CombinedField を返し、ChildFieldFactories が生成されることを確認

// T5: Sealed Interface (object のみ)
sealed interface SimpleSealed {
    data object A : SimpleSealed
    data object B : SimpleSealed
}
// → Fields.simpleSealed() が PolymorphicField を返すことを確認

// ========================================
// Phase 2B: エッジケース
// ========================================

// T6: Data Class (ネスト 2階層)
data class Level2Parent(val child: Level2Child)
data class Level2Child(val value: String)

// T7: Sealed Interface (data class サブクラス含む)
sealed interface MixedSealed {
    data object Loading : MixedSealed
    data class Success(val data: String) : MixedSealed
}

// T8: Data Class (サポート外の型を含む)
// Note: java.time.LocalDateTime は Kotlin Multiplatform では使用不可のため、
// Any 型をサポート外の型として使用
data class WithUnsupported(
    val name: String,
    val unknown: Any, // サポート外
)

// T9: ネスト 3階層
data class Level3Root(val l2: Level3Middle)
data class Level3Middle(val l3: Level3Leaf)
data class Level3Leaf(val value: Int)

// T10: Sealed の中に Sealed
sealed interface OuterSealed {
    data object Idle : OuterSealed
    data class WithInner(val inner: InnerSealed) : OuterSealed
}
sealed interface InnerSealed {
    data object A : InnerSealed
    data object B : InnerSealed
}

// T11: 5階層ネスト
data class Deep1(val d2: Deep2)
data class Deep2(val d3: Deep3)
data class Deep3(val d4: Deep4)
data class Deep4(val d5: Deep5)
data class Deep5(val value: String)

// T12: internal visibility は InternalFields.kt で定義

// T13: autoLabelByTypeName = true は Fields.kt の AutoLabelFields で確認

// T14: name カスタマイズは Fields.kt の NamedFields で確認
