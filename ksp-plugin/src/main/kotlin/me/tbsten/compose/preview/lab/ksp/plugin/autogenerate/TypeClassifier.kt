package me.tbsten.compose.preview.lab.ksp.plugin.autogenerate

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import me.tbsten.compose.preview.lab.ksp.plugin.autogenerate.model.PrimitiveKind
import me.tbsten.compose.preview.lab.ksp.plugin.autogenerate.model.PropertyInfo
import me.tbsten.compose.preview.lab.ksp.plugin.autogenerate.model.SubclassInfo
import me.tbsten.compose.preview.lab.ksp.plugin.autogenerate.model.TypeClassification

/**
 * KSType を TypeClassification に分類する
 */
class TypeClassifier {
    private val cache = mutableMapOf<String, TypeClassification>()

    fun classify(type: KSType): TypeClassification {
        val qualifiedName = type.declaration.qualifiedName?.asString()
        if (qualifiedName != null && cache.containsKey(qualifiedName)) {
            return cache[qualifiedName]!!
        }

        val result = doClassify(type)

        if (qualifiedName != null) {
            cache[qualifiedName] = result
        }

        return result
    }

    private fun doClassify(type: KSType): TypeClassification {
        val qualifiedName = type.declaration.qualifiedName?.asString()

        // 1. プリミティブ型チェック
        PrimitiveKind.fromQualifiedName(qualifiedName)?.let { kind ->
            return TypeClassification.Primitive(type, kind)
        }

        val declaration = type.declaration as? KSClassDeclaration
            ?: return TypeClassification.Unsupported(type, "Not a class declaration")

        // 2. Enum チェック
        if (declaration.classKind == ClassKind.ENUM_CLASS) {
            val entries = declaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }
                .map { it.simpleName.asString() }
                .toList()
            return TypeClassification.Enum(type, declaration, entries)
        }

        // 3. Object チェック (object または data object)
        if (declaration.classKind == ClassKind.OBJECT) {
            val isDataObject = declaration.modifiers.contains(Modifier.DATA)
            return TypeClassification.Object(type, declaration, isDataObject)
        }

        // 4. Sealed チェック
        if (declaration.modifiers.contains(Modifier.SEALED)) {
            val subclasses = declaration.getSealedSubclasses()
                .map { subclass ->
                    SubclassInfo(
                        name = subclass.simpleName.asString(),
                        declaration = subclass,
                        typeClassification = classify(subclass.asType(emptyList())),
                    )
                }
                .toList()
            return TypeClassification.Sealed(type, declaration, subclasses)
        }

        // 5. Data Class チェック
        if (declaration.modifiers.contains(Modifier.DATA)) {
            val constructor = declaration.primaryConstructor
                ?: return TypeClassification.Unsupported(type, "Data class without primary constructor")

            val properties = constructor.parameters.mapNotNull { param ->
                val propName = param.name?.asString() ?: return@mapNotNull null
                val propType = param.type.resolve()
                PropertyInfo(
                    name = propName,
                    type = propType,
                    typeClassification = classify(propType),
                )
            }
            return TypeClassification.DataClass(type, declaration, properties)
        }

        // 6. その他はサポート外
        return TypeClassification.Unsupported(type, "Unsupported type: ${declaration.classKind}")
    }
}
