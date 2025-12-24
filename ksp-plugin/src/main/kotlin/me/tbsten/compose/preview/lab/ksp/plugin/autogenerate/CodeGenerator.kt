package me.tbsten.compose.preview.lab.ksp.plugin.autogenerate

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Visibility
import me.tbsten.compose.preview.lab.ksp.plugin.autogenerate.model.PropertyInfo
import me.tbsten.compose.preview.lab.ksp.plugin.autogenerate.model.SubclassInfo
import me.tbsten.compose.preview.lab.ksp.plugin.autogenerate.model.TypeClassification

/**
 * 自動生成するフィールドのリクエスト情報
 */
internal data class FieldGenerationRequest(
    val targetObject: KSClassDeclaration,
    val targetType: KSType,
    val typeClassification: TypeClassification,
    val functionName: String,
    val autoLabelByTypeName: Boolean,
)

/**
 * コード生成を行うクラス
 */
internal class CodeGenerator {
    private val imports = mutableSetOf<String>()
    private val generatedCode = StringBuilder()

    fun generate(requests: List<FieldGenerationRequest>): String {
        if (requests.isEmpty()) return ""

        val targetObject = requests.first().targetObject
        val packageName = targetObject.packageName.asString()
        val visibility = getVisibilityModifier(targetObject)

        imports.clear()
        generatedCode.clear()

        // 基本 import
        imports.add("me.tbsten.compose.preview.lab.MutablePreviewLabField")
        imports.add("me.tbsten.compose.preview.lab.PreviewLabField")

        // 各リクエストに対するコード生成
        requests.forEach { request ->
            generateForRequest(request, visibility)
        }

        // ExperimentalComposePreviewLabApi への OptIn を追加
        imports.add("me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi")

        // ファイル全体の組み立て
        return buildString {
            appendLine("@file:Suppress(\"RedundantVisibilityModifier\", \"unused\")")
            appendLine("@file:OptIn(ExperimentalComposePreviewLabApi::class)")
            appendLine()
            appendLine("package $packageName")
            appendLine()
            imports.sorted().forEach { appendLine("import $it") }
            appendLine()
            append(generatedCode)
        }
    }

    private fun generateForRequest(request: FieldGenerationRequest, visibility: String) {
        val typeName = request.targetType.declaration.simpleName.asString()
        val comment = """
            |/**
            | * [$typeName]
            | */
        """.trimMargin()
        generatedCode.appendLine(comment)
        generatedCode.appendLine()

        when (val classification = request.typeClassification) {
            is TypeClassification.Primitive ->
                generatePrimitive(request, classification, visibility)
            is TypeClassification.Enum ->
                generateEnum(request, classification, visibility)
            is TypeClassification.Object ->
                generateObject(request, classification, visibility)
            is TypeClassification.DataClass ->
                generateDataClass(request, classification, visibility)
            is TypeClassification.Sealed ->
                generateSealed(request, classification, visibility)
            is TypeClassification.Unsupported ->
                generateUnsupported(request, classification, visibility)
        }

        generatedCode.appendLine()
    }

    private fun generatePrimitive(
        request: FieldGenerationRequest,
        classification: TypeClassification.Primitive,
        visibility: String,
    ) {
        val typeName = classification.kind.qualifiedName.substringAfterLast(".")
        val fieldClassName = classification.kind.fieldClassName
        imports.add("me.tbsten.compose.preview.lab.field.$fieldClassName")

        generatedCode.appendLine(
            """
            |${visibility}fun ${request.targetObject.simpleName.asString()}.${request.functionName}(
            |    label: String,
            |    initialValue: $typeName,
            |): MutablePreviewLabField<$typeName> = $fieldClassName(label, initialValue)
            """.trimMargin(),
        )
    }

    private fun generateEnum(request: FieldGenerationRequest, classification: TypeClassification.Enum, visibility: String) {
        val typeQualifiedName = classification.declaration.qualifiedName!!.asString()
        val typeSimpleName = classification.declaration.simpleName.asString()
        imports.add(typeQualifiedName)
        imports.add("me.tbsten.compose.preview.lab.field.enumField")

        generatedCode.appendLine(
            """
            |${visibility}fun ${request.targetObject.simpleName.asString()}.${request.functionName}(
            |    label: String,
            |    initialValue: $typeSimpleName,
            |): MutablePreviewLabField<$typeSimpleName> = enumField(label, initialValue)
            """.trimMargin(),
        )
    }

    private fun generateObject(
        request: FieldGenerationRequest,
        classification: TypeClassification.Object,
        visibility: String,
    ) {
        val typeQualifiedName = classification.declaration.qualifiedName!!.asString()
        val typeSimpleName = classification.declaration.simpleName.asString()
        imports.add(typeQualifiedName)
        imports.add("me.tbsten.compose.preview.lab.field.FixedField")

        generatedCode.appendLine(
            """
            |${visibility}fun ${request.targetObject.simpleName.asString()}.${request.functionName}(
            |    label: String,
            |): PreviewLabField<$typeSimpleName> = FixedField(label, $typeSimpleName)
            """.trimMargin(),
        )
    }

    private fun generateDataClass(
        request: FieldGenerationRequest,
        classification: TypeClassification.DataClass,
        visibility: String,
    ) {
        val typeQualifiedName = classification.declaration.qualifiedName!!.asString()
        val typeSimpleName = classification.declaration.simpleName.asString()
        val targetObjectName = request.targetObject.simpleName.asString()
        val properties = classification.properties

        imports.add(typeQualifiedName)
        imports.add("me.tbsten.compose.preview.lab.field.ChildFieldScope")
        imports.add("me.tbsten.compose.preview.lab.field.combined")
        imports.add("me.tbsten.compose.preview.lab.field.splitedOf")

        // 1. ChildFieldFactories object
        // Use targetObject name prefix to avoid name collision when same type is used in multiple objects
        val factoriesObjectName = "${targetObjectName}_${typeSimpleName}ChildFieldFactories"
        generatedCode.appendLine("${visibility}object $factoriesObjectName {")
        properties.forEach { prop ->
            generateChildFieldFactory(prop, visibility)
        }
        generatedCode.appendLine("}")
        generatedCode.appendLine()

        // 2. Accessor property
        generatedCode.appendLine(
            "${visibility}val $targetObjectName.${request.functionName}: $factoriesObjectName",
        )
        generatedCode.appendLine("    get() = $factoriesObjectName")
        generatedCode.appendLine()

        // 3. Factory extension function
        val labelDefault = if (request.autoLabelByTypeName) " = \"${request.functionName}\"" else ""
        generatedCode.appendLine("${visibility}fun $targetObjectName.${request.functionName}(")
        generatedCode.appendLine("    label: String$labelDefault,")
        generatedCode.appendLine("    initialValue: $typeSimpleName,")
        properties.forEach { prop ->
            val propFieldType = getFieldType(prop.typeClassification)
            val propTypeRef = getTypeReference(prop.type)
            generatedCode.appendLine(
                "    ${prop.name}Field: ChildFieldScope<$propTypeRef>.() -> " +
                    "$propFieldType<$propTypeRef> =",
            )
            generatedCode.appendLine("        ${request.functionName}.${prop.name}Field(),")
        }
        generatedCode.appendLine("): MutablePreviewLabField<$typeSimpleName> = combined(")
        generatedCode.appendLine("    label = label,")
        properties.forEachIndexed { index, prop ->
            generatedCode.appendLine(
                "    field${index + 1} = ${prop.name}Field(" +
                    "ChildFieldScope(\"${prop.name}\", initialValue.${prop.name})),",
            )
        }

        // combine lambda
        val combineParams = properties.joinToString(", ") { it.name }
        val combineBody = properties.joinToString(", ") { "${it.name} = ${it.name}" }
        generatedCode.appendLine("    combine = { $combineParams -> $typeSimpleName($combineBody) },")

        // split lambda
        val splitBody = properties.joinToString(", ") { "it.${it.name}" }
        generatedCode.appendLine("    split = { splitedOf($splitBody) },")
        generatedCode.appendLine(")")
    }

    private fun generateChildFieldFactory(prop: PropertyInfo, visibility: String) {
        val propTypeRef = getTypeReference(prop.type)
        val fieldType = getFieldType(prop.typeClassification)
        val fieldCreation = getFieldCreation(prop.typeClassification, prop.type)

        generatedCode.appendLine("    ${visibility}fun ${prop.name}Field(")
        generatedCode.appendLine(
            "        transform: ($fieldType<$propTypeRef>) -> $fieldType<$propTypeRef> = { it },",
        )
        generatedCode.appendLine("    ): ChildFieldScope<$propTypeRef>.() -> $fieldType<$propTypeRef> = {")
        generatedCode.appendLine("        $fieldCreation.let(transform)")
        generatedCode.appendLine("    }")
        generatedCode.appendLine()
    }

    private fun generateSealed(
        request: FieldGenerationRequest,
        classification: TypeClassification.Sealed,
        visibility: String,
    ) {
        val typeQualifiedName = classification.declaration.qualifiedName!!.asString()
        val typeSimpleName = classification.declaration.simpleName.asString()
        val targetObjectName = request.targetObject.simpleName.asString()
        val subclasses = classification.subclasses

        imports.add(typeQualifiedName)
        imports.add("me.tbsten.compose.preview.lab.field.ChildFieldScope")
        imports.add("me.tbsten.compose.preview.lab.field.PolymorphicField")

        // 1. ChildFieldFactories object
        // Use targetObject name prefix to avoid name collision when same type is used in multiple objects
        val factoriesObjectName = "${targetObjectName}_${typeSimpleName}ChildFieldFactories"
        generatedCode.appendLine("${visibility}object $factoriesObjectName {")
        subclasses.forEach { sub ->
            generateSealedChildFieldFactory(sub, typeSimpleName, visibility)
        }
        generatedCode.appendLine("}")
        generatedCode.appendLine()

        // 2. Accessor property
        generatedCode.appendLine(
            "${visibility}val $targetObjectName.${request.functionName}: $factoriesObjectName",
        )
        generatedCode.appendLine("    get() = $factoriesObjectName")
        generatedCode.appendLine()

        // 3. Factory extension function
        val labelDefault = if (request.autoLabelByTypeName) " = \"${request.functionName}\"" else ""
        generatedCode.appendLine("${visibility}fun $targetObjectName.${request.functionName}(")
        generatedCode.appendLine("    label: String$labelDefault,")
        generatedCode.appendLine("    initialValue: $typeSimpleName,")

        // initial* parameters and field parameters for each subclass
        subclasses.forEach { sub ->
            val subFullType = "$typeSimpleName.${sub.name}"
            val subParamName = sub.name.replaceFirstChar { it.lowercase() }
            val fieldType = getFieldType(sub.typeClassification)

            when (sub.typeClassification) {
                is TypeClassification.Object -> {
                    // object has default value
                    generatedCode.appendLine("    initial${sub.name}: $subFullType = $subFullType,")
                }
                else -> {
                    // non-object is required parameter
                    generatedCode.appendLine("    initial${sub.name}: $subFullType,")
                }
            }
            generatedCode.appendLine(
                "    ${subParamName}Field: ChildFieldScope<$subFullType>.() -> " +
                    "$fieldType<$subFullType> =",
            )
            generatedCode.appendLine("        ${request.functionName}.${subParamName}Field(),")
        }

        generatedCode.appendLine("): MutablePreviewLabField<$typeSimpleName> = PolymorphicField(")
        generatedCode.appendLine("    label = label,")
        generatedCode.appendLine("    initialValue = initialValue,")
        generatedCode.appendLine("    fields = listOf(")
        subclasses.forEach { sub ->
            val subParamName = sub.name.replaceFirstChar { it.lowercase() }
            generatedCode.appendLine(
                "        ${subParamName}Field(ChildFieldScope(\"$subParamName\", initial${sub.name})),",
            )
        }
        generatedCode.appendLine("    ),")
        generatedCode.appendLine(")")
    }

    private fun generateSealedChildFieldFactory(sub: SubclassInfo, parentTypeName: String, visibility: String) {
        val subFullType = "$parentTypeName.${sub.name}"
        val fieldType = getFieldType(sub.typeClassification)
        val subParamName = sub.name.replaceFirstChar { it.lowercase() }
        val fieldCreation = getSealedSubclassFieldCreation(sub, parentTypeName)

        generatedCode.appendLine("    ${visibility}fun ${subParamName}Field(")
        generatedCode.appendLine(
            "        transform: ($fieldType<$subFullType>) -> $fieldType<$subFullType> = { it },",
        )
        generatedCode.appendLine(
            "    ): ChildFieldScope<$subFullType>.() -> $fieldType<$subFullType> = {",
        )
        generatedCode.appendLine("        $fieldCreation.let(transform)")
        generatedCode.appendLine("    }")
        generatedCode.appendLine()
    }

    private fun generateUnsupported(
        request: FieldGenerationRequest,
        classification: TypeClassification.Unsupported,
        visibility: String,
    ) {
        val typeRef = getTypeReference(classification.ksType)
        val targetObjectName = request.targetObject.simpleName.asString()

        generatedCode.appendLine("// Unsupported type: ${classification.reason}")
        generatedCode.appendLine(
            "// ${visibility}fun $targetObjectName.${request.functionName}(...): " +
                "MutablePreviewLabField<$typeRef>",
        )
    }

    private fun getVisibilityModifier(declaration: KSClassDeclaration): String = when (declaration.getVisibility()) {
        Visibility.PUBLIC -> "public "
        Visibility.INTERNAL -> "internal "
        Visibility.PRIVATE -> "private "
        Visibility.PROTECTED -> "protected "
        else -> "public "
    }

    private fun getFieldType(classification: TypeClassification): String = when (classification) {
        is TypeClassification.Object -> "PreviewLabField"
        else -> "MutablePreviewLabField"
    }

    private fun getTypeReference(type: KSType): String {
        val declaration = type.declaration
        val qualifiedName = declaration.qualifiedName?.asString()
        if (qualifiedName != null) {
            imports.add(qualifiedName)
        }
        return declaration.simpleName.asString()
    }

    private fun getFieldCreation(
        classification: TypeClassification,
        type: KSType,
        labelExpr: String = "label",
        initialValueExpr: String = "initialValue",
    ): String = when (classification) {
        is TypeClassification.Primitive -> {
            imports.add("me.tbsten.compose.preview.lab.field.${classification.kind.fieldClassName}")
            "${classification.kind.fieldClassName}($labelExpr, $initialValueExpr)"
        }
        is TypeClassification.Enum -> {
            imports.add("me.tbsten.compose.preview.lab.field.enumField")
            "enumField($labelExpr, $initialValueExpr)"
        }
        is TypeClassification.Object -> {
            imports.add("me.tbsten.compose.preview.lab.field.FixedField")
            val typeName = classification.declaration.simpleName.asString()
            "FixedField($labelExpr, $typeName)"
        }
        is TypeClassification.DataClass -> {
            generateNestedDataClassField(classification, initialValueExpr)
        }
        is TypeClassification.Sealed -> {
            generateNestedSealedField(classification, initialValueExpr)
        }
        is TypeClassification.Unsupported -> {
            "TODO(\"Unsupported type: ${classification.reason}\")"
        }
    }

    private fun getSealedSubclassFieldCreation(sub: SubclassInfo, parentTypeName: String): String {
        val subFullType = "$parentTypeName.${sub.name}"

        return when (val classification = sub.typeClassification) {
            is TypeClassification.Object -> {
                imports.add("me.tbsten.compose.preview.lab.field.FixedField")
                "FixedField(label, $subFullType)"
            }
            is TypeClassification.DataClass -> {
                generateNestedDataClassFieldForSealed(classification, subFullType, "label")
            }
            else -> {
                "/* Unsupported sealed subclass type */"
            }
        }
    }

    private fun generateNestedDataClassField(
        classification: TypeClassification.DataClass,
        parentInitialValueExpr: String,
        labelExpr: String = "label",
    ): String {
        val typeName = classification.declaration.simpleName.asString()
        val properties = classification.properties
        if (properties.isEmpty()) return "/* Empty data class */"

        imports.add("me.tbsten.compose.preview.lab.field.combined")
        imports.add("me.tbsten.compose.preview.lab.field.splitedOf")

        val fieldArgs = properties.mapIndexed { index, prop ->
            val propLabel = "\"${prop.name}\""
            val propInitialValue = "$parentInitialValueExpr.${prop.name}"
            val fieldCreation = getNestedFieldCreation(prop.typeClassification, prop.type, propLabel, propInitialValue)
            "field${index + 1} = $fieldCreation"
        }

        val combineParams = properties.joinToString(", ") { it.name }
        val combineBody = properties.joinToString(", ") { "${it.name} = ${it.name}" }
        val splitBody = properties.joinToString(", ") { "it.${it.name}" }

        return buildString {
            append("combined(\n")
            append("            label = $labelExpr,\n")
            fieldArgs.forEach { append("            $it,\n") }
            append("            combine = { $combineParams -> $typeName($combineBody) },\n")
            append("            split = { splitedOf($splitBody) },\n")
            append("        )")
        }
    }

    private fun getNestedFieldCreation(
        classification: TypeClassification,
        type: KSType,
        labelExpr: String,
        initialValueExpr: String,
    ): String = when (classification) {
        is TypeClassification.Primitive -> {
            imports.add("me.tbsten.compose.preview.lab.field.${classification.kind.fieldClassName}")
            "${classification.kind.fieldClassName}($labelExpr, $initialValueExpr)"
        }
        is TypeClassification.Enum -> {
            imports.add("me.tbsten.compose.preview.lab.field.enumField")
            "enumField($labelExpr, $initialValueExpr)"
        }
        is TypeClassification.Object -> {
            imports.add("me.tbsten.compose.preview.lab.field.FixedField")
            val typeName = classification.declaration.simpleName.asString()
            "FixedField($labelExpr, $typeName)"
        }
        is TypeClassification.DataClass -> {
            generateNestedDataClassField(classification, initialValueExpr, labelExpr)
        }
        is TypeClassification.Sealed -> {
            generateNestedSealedField(classification, initialValueExpr, labelExpr)
        }
        is TypeClassification.Unsupported -> {
            "TODO(\"Unsupported type: ${classification.reason}\")"
        }
    }

    private fun generateNestedDataClassFieldForSealed(
        classification: TypeClassification.DataClass,
        subFullType: String,
        labelExpr: String = "label",
    ): String {
        val properties = classification.properties
        if (properties.isEmpty()) return "/* Empty data class */"

        imports.add("me.tbsten.compose.preview.lab.field.combined")
        imports.add("me.tbsten.compose.preview.lab.field.splitedOf")

        val fieldArgs = properties.mapIndexed { index, prop ->
            val propLabel = "\"${prop.name}\""
            val propInitialValue = "initialValue.${prop.name}"
            val fieldCreation = getNestedFieldCreation(prop.typeClassification, prop.type, propLabel, propInitialValue)
            "field${index + 1} = $fieldCreation"
        }

        val combineParams = properties.joinToString(", ") { it.name }
        val combineBody = properties.joinToString(", ") { "${it.name} = ${it.name}" }
        val splitBody = properties.joinToString(", ") { "it.${it.name}" }

        return buildString {
            append("combined(\n")
            append("            label = $labelExpr,\n")
            fieldArgs.forEach { append("            $it,\n") }
            append("            combine = { $combineParams -> $subFullType($combineBody) },\n")
            append("            split = { splitedOf($splitBody) },\n")
            append("        )")
        }
    }

    private fun generateNestedSealedField(
        classification: TypeClassification.Sealed,
        parentInitialValueExpr: String,
        labelExpr: String = "label",
    ): String {
        // For nested sealed interfaces, generate PolymorphicField inline
        imports.add("me.tbsten.compose.preview.lab.field.PolymorphicField")

        val typeName = classification.declaration.simpleName.asString()
        val subclasses = classification.subclasses

        val fieldsCode = subclasses.map { sub ->
            val subFullType = "$typeName.${sub.name}"
            val subLabel = "\"${sub.name.replaceFirstChar { it.lowercase() }}\""
            when (sub.typeClassification) {
                is TypeClassification.Object -> {
                    imports.add("me.tbsten.compose.preview.lab.field.FixedField")
                    "FixedField($subLabel, $subFullType)"
                }
                is TypeClassification.DataClass -> {
                    // Generate combined field for data class subclass
                    generateNestedDataClassFieldForSealedInline(
                        sub.typeClassification,
                        subFullType,
                        subLabel,
                    )
                }
                else -> "TODO(\"Complex subclass ${sub.name}\")"
            }
        }

        return buildString {
            append("PolymorphicField(\n")
            append("            label = $labelExpr,\n")
            append("            initialValue = $parentInitialValueExpr,\n")
            append("            fields = listOf(\n")
            fieldsCode.forEach { append("                $it,\n") }
            append("            ),\n")
            append("        )")
        }
    }

    private fun generateNestedDataClassFieldForSealedInline(
        classification: TypeClassification.DataClass,
        subFullType: String,
        labelExpr: String,
    ): String {
        val properties = classification.properties
        if (properties.isEmpty()) {
            imports.add("me.tbsten.compose.preview.lab.field.FixedField")
            return "FixedField($labelExpr, $subFullType)"
        }

        imports.add("me.tbsten.compose.preview.lab.field.combined")
        imports.add("me.tbsten.compose.preview.lab.field.splitedOf")

        // Note: For inline nested sealed, we use simple default values since we can't access parent's initialValue
        val fieldArgs = properties.mapIndexed { index, prop ->
            val propLabel = "\"${prop.name}\""
            val defaultValue = getDefaultValueForType(prop.typeClassification)
            "field${index + 1} = ${getSimpleFieldCreation(prop.typeClassification, propLabel, defaultValue)}"
        }

        val combineParams = properties.joinToString(", ") { it.name }
        val combineBody = properties.joinToString(", ") { "${it.name} = ${it.name}" }
        val splitBody = properties.joinToString(", ") { "it.${it.name}" }

        return buildString {
            append("combined(\n")
            append("                label = $labelExpr,\n")
            fieldArgs.forEach { append("                $it,\n") }
            append("                combine = { $combineParams -> $subFullType($combineBody) },\n")
            append("                split = { splitedOf($splitBody) },\n")
            append("            )")
        }
    }

    private fun getSimpleFieldCreation(
        classification: TypeClassification,
        labelExpr: String,
        defaultValueExpr: String,
    ): String = when (classification) {
        is TypeClassification.Primitive -> {
            imports.add("me.tbsten.compose.preview.lab.field.${classification.kind.fieldClassName}")
            "${classification.kind.fieldClassName}($labelExpr, $defaultValueExpr)"
        }
        is TypeClassification.Enum -> {
            imports.add("me.tbsten.compose.preview.lab.field.enumField")
            "enumField($labelExpr, $defaultValueExpr)"
        }
        is TypeClassification.Object -> {
            imports.add("me.tbsten.compose.preview.lab.field.FixedField")
            val typeName = classification.declaration.simpleName.asString()
            "FixedField($labelExpr, $typeName)"
        }
        else -> "TODO(\"Nested complex type\")"
    }

    private fun getDefaultValueForType(classification: TypeClassification): String = when (classification) {
        is TypeClassification.Primitive -> when (classification.kind.qualifiedName) {
            "kotlin.String" -> "\"\""
            "kotlin.Int" -> "0"
            "kotlin.Long" -> "0L"
            "kotlin.Float" -> "0f"
            "kotlin.Double" -> "0.0"
            "kotlin.Boolean" -> "false"
            "kotlin.Byte" -> "0"
            else -> "TODO()"
        }
        is TypeClassification.Enum -> {
            val firstEntry = classification.entries.firstOrNull() ?: "TODO()"
            "${classification.declaration.simpleName.asString()}.$firstEntry"
        }
        is TypeClassification.Object -> classification.declaration.simpleName.asString()
        else -> "TODO()"
    }
}
