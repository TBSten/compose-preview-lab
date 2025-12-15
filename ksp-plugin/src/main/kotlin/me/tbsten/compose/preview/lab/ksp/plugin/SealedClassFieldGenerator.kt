package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import java.io.OutputStreamWriter

/**
 * Generates a PolymorphicField extension function for a sealed interface or sealed class.
 */
internal fun generatePolymorphicFieldForSealedClass(
    classDeclaration: KSClassDeclaration,
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
) {
    // Find companion object
    val companionObject = classDeclaration.declarations
        .filterIsInstance<KSClassDeclaration>()
        .firstOrNull { it.isCompanionObject }

    if (companionObject == null) {
        logger.error("@GenerateCombinedField on sealed type requires a companion object", classDeclaration)
        return
    }

    // Get all sealed subclasses
    val sealedSubclasses = classDeclaration.getSealedSubclasses().toList()

    if (sealedSubclasses.isEmpty()) {
        logger.error("@GenerateCombinedField on sealed type requires at least one subclass", classDeclaration)
        return
    }

    val className = classDeclaration.simpleName.asString()
    val packageName = classDeclaration.packageName.asString()
    val qualifiedClassName = classDeclaration.qualifiedName?.asString() ?: className

    // Generate the file
    val fileName = "${className}GeneratedField"
    val file = codeGenerator.createNewFile(
        dependencies = Dependencies(true, classDeclaration.containingFile!!),
        packageName = packageName,
        fileName = fileName,
    )

    OutputStreamWriter(file).use { writer ->
        writer.write(
            generatePolymorphicFieldCode(
                className,
                qualifiedClassName,
                packageName,
                sealedSubclasses,
                logger,
            ),
        )
    }
}

/**
 * Generates the PolymorphicField code for a sealed type.
 */
private fun generatePolymorphicFieldCode(
    className: String,
    qualifiedClassName: String,
    packageName: String,
    sealedSubclasses: List<KSClassDeclaration>,
    logger: KSPLogger,
): String {
    val imports = mutableSetOf<String>()

    imports.add("me.tbsten.compose.preview.lab.MutablePreviewLabField")
    imports.add("me.tbsten.compose.preview.lab.field.PolymorphicField")
    imports.add("me.tbsten.compose.preview.lab.field.FixedField")

    // Generate field entries for each subclass
    val fieldEntries = sealedSubclasses.map { subclass ->
        generateSealedSubclassFieldEntry(subclass, className, imports, logger)
    }

    val code = buildString {
        appendLine("package $packageName")
        appendLine()

        // Add imports
        imports.sorted().forEach { import ->
            appendLine("import $import")
        }
        appendLine()

        // Generate the extension function
        appendLine("/**")
        appendLine(" * Auto-generated field function for [$qualifiedClassName].")
        appendLine(" * ")
        appendLine(" * Creates a MutablePreviewLabField<$className> with PolymorphicField.")
        appendLine(" */")
        appendLine("fun $className.Companion.field(")
        appendLine("    label: String,")
        appendLine("    initialValue: $className,")
        appendLine("): MutablePreviewLabField<$className> = PolymorphicField(")
        appendLine("    label = label,")
        appendLine("    initialValue = initialValue,")
        appendLine("    fields = listOf(")
        fieldEntries.forEach { entry ->
            appendLine("        $entry,")
        }
        appendLine("    ),")
        appendLine(")")
    }

    return code
}

/**
 * Generates a field entry for a single sealed subclass.
 */
private fun generateSealedSubclassFieldEntry(
    subclass: KSClassDeclaration,
    sealedClassName: String,
    imports: MutableSet<String>,
    logger: KSPLogger,
): String {
    val subclassName = subclass.simpleName.asString()
    val isObject = subclass.classKind == com.google.devtools.ksp.symbol.ClassKind.OBJECT
    val isDataClass = subclass.modifiers.contains(Modifier.DATA)

    return when {
        // data object or object -> FixedField
        isObject -> {
            "FixedField(label = \"$subclassName\", value = $sealedClassName.$subclassName)"
        }
        // data class -> combined field
        isDataClass -> {
            val properties = subclass.primaryConstructor?.parameters ?: emptyList()

            if (properties.isEmpty()) {
                // Data class with no properties -> FixedField
                "FixedField(label = \"$subclassName\", value = $sealedClassName.$subclassName)"
            } else if (properties.size > 10) {
                logger.error(
                    "Sealed subclass $subclassName has more than 10 properties, which is not supported",
                    subclass,
                )
                "FixedField(label = \"$subclassName\", value = $sealedClassName.$subclassName)"
            } else {
                generateCombinedFieldForSealedSubclass(
                    subclassName,
                    sealedClassName,
                    properties,
                    imports,
                    logger,
                )
            }
        }
        else -> {
            logger.warn("Unsupported sealed subclass type: $subclassName. Using FixedField with null.")
            "FixedField(label = \"$subclassName\", value = null)"
        }
    }
}

/**
 * Generates a combined field for a data class sealed subclass.
 */
private fun generateCombinedFieldForSealedSubclass(
    subclassName: String,
    sealedClassName: String,
    properties: List<com.google.devtools.ksp.symbol.KSValueParameter>,
    imports: MutableSet<String>,
    logger: KSPLogger,
): String {
    imports.add("me.tbsten.compose.preview.lab.field.combined")
    imports.add("me.tbsten.compose.preview.lab.field.splitedOf")

    // Build field creators for each property
    val fieldCreators = properties.mapIndexed { index, param ->
        val paramName = param.name?.asString() ?: "param$index"
        val paramType = param.type.resolve()
        generateFieldCreatorForSealedSubclass(paramName, paramType, imports, logger)
    }

    // Build combine lambda parameters
    val combineParams = properties.mapIndexed { index, param ->
        param.name?.asString() ?: "param$index"
    }.joinToString(", ")

    // Build combine call
    val combineCall = properties.joinToString(", ") { param ->
        val paramName = param.name?.asString() ?: ""
        "$paramName = $paramName"
    }

    // Build split params
    val splitParams = properties.joinToString(", ") { param ->
        "it.${param.name?.asString()}"
    }

    return buildString {
        append("combined(")
        append("label = \"$subclassName\", ")
        fieldCreators.forEachIndexed { index, fieldCreator ->
            append("field${index + 1} = $fieldCreator, ")
        }
        append("combine = { $combineParams -> $sealedClassName.$subclassName($combineCall) }, ")
        append("split = { splitedOf($splitParams) }")
        append(")")
    }
}

/**
 * Generates a field creator for a property of a sealed subclass.
 * This is a simplified version that uses default initial values.
 */
private fun generateFieldCreatorForSealedSubclass(
    paramName: String,
    paramType: KSType,
    imports: MutableSet<String>,
    logger: KSPLogger,
): String {
    // Check if the type is nullable
    val isNullable = paramType.isMarkedNullable
    val nonNullType = if (isNullable) paramType.makeNotNullable() else paramType

    val typeName = nonNullType.declaration.simpleName.asString()
    val qualifiedTypeName = nonNullType.declaration.qualifiedName?.asString()
    val typeDeclaration = nonNullType.declaration as? KSClassDeclaration

    // Generate the base field creator
    val baseFieldCreator = generateBaseFieldCreatorForSealedSubclass(
        paramName,
        typeName,
        qualifiedTypeName,
        typeDeclaration,
        imports,
        logger,
    )

    // Wrap with nullable() if needed
    return if (isNullable) {
        imports.add("me.tbsten.compose.preview.lab.field.nullable")
        "$baseFieldCreator.nullable(initialValue = null)"
    } else {
        baseFieldCreator
    }
}

/**
 * Generates the base field creator for a sealed subclass property.
 */
private fun generateBaseFieldCreatorForSealedSubclass(
    paramName: String,
    typeName: String,
    qualifiedTypeName: String?,
    typeDeclaration: KSClassDeclaration?,
    imports: MutableSet<String>,
    logger: KSPLogger,
): String {
    // 1. Check for Enum type
    if (typeDeclaration != null && typeDeclaration.classKind == com.google.devtools.ksp.symbol.ClassKind.ENUM_CLASS) {
        imports.add("me.tbsten.compose.preview.lab.field.EnumField")
        val qualifiedName = typeDeclaration.qualifiedName?.asString() ?: typeName
        imports.add(qualifiedName)
        val firstEntry = typeDeclaration.declarations
            .filterIsInstance<KSClassDeclaration>()
            .firstOrNull { it.classKind == com.google.devtools.ksp.symbol.ClassKind.ENUM_ENTRY }
        val defaultValue = if (firstEntry != null) "$typeName.${firstEntry.simpleName.asString()}" else "TODO()"
        return "EnumField<$typeName>(label = \"$paramName\", initialValue = $defaultValue)"
    }

    // 2. Check for Value class
    val isValueClass = typeDeclaration != null &&
        (
            typeDeclaration.modifiers.contains(Modifier.INLINE) ||
                typeDeclaration.modifiers.contains(Modifier.VALUE) ||
                typeDeclaration.annotations.any {
                    val annotationName = it.annotationType.resolve().declaration.qualifiedName?.asString()
                    annotationName == "kotlin.jvm.JvmInline"
                }
            )

    if (isValueClass && typeDeclaration != null) {
        val underlyingProperty = typeDeclaration.getAllProperties().firstOrNull()
        if (underlyingProperty != null) {
            val underlyingType = underlyingProperty.type.resolve()
            val propertyName = underlyingProperty.simpleName.asString()
            val defaultValue = getDefaultValueForType(underlyingType)

            imports.add("me.tbsten.compose.preview.lab.field.TransformField")
            val qualifiedName = typeDeclaration.qualifiedName?.asString() ?: typeName
            imports.add(qualifiedName)

            val underlyingFieldType = getPrimitiveFieldType(
                underlyingType.declaration.qualifiedName?.asString(),
                imports,
            ) ?: "StringField".also { imports.add("me.tbsten.compose.preview.lab.field.StringField") }

            return "TransformField(" +
                "label = \"$paramName\", " +
                "baseField = $underlyingFieldType(label = \"$paramName\", initialValue = $defaultValue), " +
                "transform = { $typeName(it) }, " +
                "reverse = { it.$propertyName }" +
                ")"
        }
    }

    // 3. Try to map primitive types to field types
    return when (qualifiedTypeName) {
        "kotlin.String" -> {
            imports.add("me.tbsten.compose.preview.lab.field.StringField")
            "StringField(label = \"$paramName\", initialValue = \"\")"
        }
        "kotlin.Int" -> {
            imports.add("me.tbsten.compose.preview.lab.field.IntField")
            "IntField(label = \"$paramName\", initialValue = 0)"
        }
        "kotlin.Long" -> {
            imports.add("me.tbsten.compose.preview.lab.field.LongField")
            "LongField(label = \"$paramName\", initialValue = 0L)"
        }
        "kotlin.Float" -> {
            imports.add("me.tbsten.compose.preview.lab.field.FloatField")
            "FloatField(label = \"$paramName\", initialValue = 0f)"
        }
        "kotlin.Double" -> {
            imports.add("me.tbsten.compose.preview.lab.field.DoubleField")
            "DoubleField(label = \"$paramName\", initialValue = 0.0)"
        }
        "kotlin.Boolean" -> {
            imports.add("me.tbsten.compose.preview.lab.field.BooleanField")
            "BooleanField(label = \"$paramName\", initialValue = false)"
        }
        "kotlin.Byte" -> {
            imports.add("me.tbsten.compose.preview.lab.field.ByteField")
            "ByteField(label = \"$paramName\", initialValue = 0)"
        }
        "androidx.compose.ui.unit.Dp" -> {
            imports.add("me.tbsten.compose.preview.lab.field.DpField")
            imports.add("androidx.compose.ui.unit.dp")
            "DpField(label = \"$paramName\", initialValue = 0.dp)"
        }
        "androidx.compose.ui.graphics.Color" -> {
            imports.add("me.tbsten.compose.preview.lab.field.ColorField")
            imports.add("androidx.compose.ui.graphics.Color")
            "ColorField(label = \"$paramName\", initialValue = Color.Black)"
        }
        else -> {
            // Fallback to StringField
            logger.warn(
                "Unsupported type $qualifiedTypeName for sealed subclass property $paramName. " +
                    "Falling back to StringField.",
            )
            imports.add("me.tbsten.compose.preview.lab.field.StringField")
            "StringField(label = \"$paramName\", initialValue = \"\")"
        }
    }
}
