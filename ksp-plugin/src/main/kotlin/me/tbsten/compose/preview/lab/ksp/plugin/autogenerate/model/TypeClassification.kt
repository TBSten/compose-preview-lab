package me.tbsten.compose.preview.lab.ksp.plugin.autogenerate.model

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

/**
 * 型の分類結果を表す sealed interface
 */
sealed interface TypeClassification {
    val ksType: KSType

    /**
     * プリミティブ型 (String, Int, Long, Float, Double, Boolean, Byte)
     */
    data class Primitive(override val ksType: KSType, val kind: PrimitiveKind,) : TypeClassification

    /**
     * Enum 型
     */
    data class Enum(override val ksType: KSType, val declaration: KSClassDeclaration, val entries: List<String>,) :
        TypeClassification

    /**
     * Object 型 (object または data object)
     */
    data class Object(override val ksType: KSType, val declaration: KSClassDeclaration, val isDataObject: Boolean,) :
        TypeClassification

    /**
     * Data Class 型
     */
    data class DataClass(
        override val ksType: KSType,
        val declaration: KSClassDeclaration,
        val properties: List<PropertyInfo>,
    ) : TypeClassification

    /**
     * Sealed Interface/Class 型
     */
    data class Sealed(override val ksType: KSType, val declaration: KSClassDeclaration, val subclasses: List<SubclassInfo>,) :
        TypeClassification

    /**
     * サポート外の型
     */
    data class Unsupported(override val ksType: KSType, val reason: String,) : TypeClassification
}

/**
 * プリミティブ型の種類
 */
enum class PrimitiveKind(val qualifiedName: String, val fieldClassName: String,) {
    STRING("kotlin.String", "StringField"),
    INT("kotlin.Int", "IntField"),
    LONG("kotlin.Long", "LongField"),
    FLOAT("kotlin.Float", "FloatField"),
    DOUBLE("kotlin.Double", "DoubleField"),
    BOOLEAN("kotlin.Boolean", "BooleanField"),
    BYTE("kotlin.Byte", "ByteField"),
    ;

    companion object {
        fun fromQualifiedName(name: String?): PrimitiveKind? = entries.find { it.qualifiedName == name }
    }
}

/**
 * Data Class のプロパティ情報
 */
data class PropertyInfo(val name: String, val type: KSType, val typeClassification: TypeClassification,)

/**
 * Sealed のサブクラス情報
 */
data class SubclassInfo(val name: String, val declaration: KSClassDeclaration, val typeClassification: TypeClassification,)
