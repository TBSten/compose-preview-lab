package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

internal const val GenerateCombinedFieldAnnotation =
    "me.tbsten.compose.preview.lab.generatecombinedfield.GenerateCombinedField"

/**
 * Returns the default value for a given type.
 */
internal fun getDefaultValueForType(type: KSType): String {
    return when (type.declaration.qualifiedName?.asString()) {
        "kotlin.String" -> "\"\""
        "kotlin.Int" -> "0"
        "kotlin.Long" -> "0L"
        "kotlin.Float" -> "0f"
        "kotlin.Double" -> "0.0"
        "kotlin.Boolean" -> "false"
        "kotlin.Byte" -> "0"
        else -> {
            // For enum types, use the first entry
            val typeDecl = type.declaration as? KSClassDeclaration
            if (typeDecl != null && typeDecl.classKind == com.google.devtools.ksp.symbol.ClassKind.ENUM_CLASS) {
                val firstEntry = typeDecl.declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .firstOrNull { it.classKind == com.google.devtools.ksp.symbol.ClassKind.ENUM_ENTRY }
                if (firstEntry != null) {
                    val typeName = typeDecl.simpleName.asString()
                    val entryName = firstEntry.simpleName.asString()
                    return "$typeName.$entryName"
                }
            }
            // Default fallback
            "null"
        }
    }
}

/**
 * Maps a qualified type name to its corresponding field type.
 * Returns null if the type is not a recognized primitive type.
 */
internal fun getPrimitiveFieldType(qualifiedTypeName: String?, imports: MutableSet<String>): String? =
    when (qualifiedTypeName) {
        "kotlin.String" -> {
            imports.add("me.tbsten.compose.preview.lab.field.StringField")
            "StringField"
        }
        "kotlin.Int" -> {
            imports.add("me.tbsten.compose.preview.lab.field.IntField")
            "IntField"
        }
        "kotlin.Long" -> {
            imports.add("me.tbsten.compose.preview.lab.field.LongField")
            "LongField"
        }
        "kotlin.Float" -> {
            imports.add("me.tbsten.compose.preview.lab.field.FloatField")
            "FloatField"
        }
        "kotlin.Double" -> {
            imports.add("me.tbsten.compose.preview.lab.field.DoubleField")
            "DoubleField"
        }
        "kotlin.Boolean" -> {
            imports.add("me.tbsten.compose.preview.lab.field.BooleanField")
            "BooleanField"
        }
        "kotlin.Byte" -> {
            imports.add("me.tbsten.compose.preview.lab.field.ByteField")
            "ByteField"
        }
        else -> null
    }
