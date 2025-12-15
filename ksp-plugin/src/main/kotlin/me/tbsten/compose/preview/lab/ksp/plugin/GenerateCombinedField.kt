package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import java.io.OutputStreamWriter

private const val GenerateCombinedFieldAnnotation =
    "me.tbsten.compose.preview.lab.generatecombinedfield.GenerateCombinedField"

internal fun generateCombinedFields(resolver: Resolver, codeGenerator: CodeGenerator, logger: KSPLogger,) {
    val annotatedClasses = resolver
        .getSymbolsWithAnnotation(GenerateCombinedFieldAnnotation)
        .filterIsInstance<KSClassDeclaration>()

    // Track which classes have already been processed to avoid duplicate generation
    val processedClasses = mutableSetOf<String>()
    // Queue of classes to process
    val toProcess = mutableListOf<KSClassDeclaration>()

    // Start with annotated classes
    toProcess.addAll(annotatedClasses)

    while (toProcess.isNotEmpty()) {
        val classDeclaration = toProcess.removeAt(0)
        val qualifiedName = classDeclaration.qualifiedName?.asString() ?: continue

        // Skip if already processed
        if (qualifiedName in processedClasses) continue
        processedClasses.add(qualifiedName)

        try {
            // Check if it's a sealed interface/class
            if (classDeclaration.modifiers.contains(Modifier.SEALED)) {
                generatePolymorphicFieldForSealedClass(classDeclaration, codeGenerator, logger)
            } else {
                // Find dependent data classes (those used in properties)
                val dependentClasses = findDependentDataClasses(classDeclaration, logger)

                // Add dependent classes to the processing queue
                dependentClasses.forEach { dependent ->
                    val dependentQualified = dependent.qualifiedName?.asString()
                    if (dependentQualified != null && dependentQualified !in processedClasses) {
                        toProcess.add(dependent)
                    }
                }

                // Generate field for current class
                generateCombinedFieldForClass(classDeclaration, codeGenerator, logger)
            }
        } catch (e: Exception) {
            logger.error(
                "Failed to generate field for ${classDeclaration.qualifiedName?.asString()}: ${e.message}",
                classDeclaration,
            )
        }
    }
}

/**
 * Finds all data classes with companion objects that are used as property types
 * in the given class declaration
 */
private fun findDependentDataClasses(classDeclaration: KSClassDeclaration, logger: KSPLogger,): List<KSClassDeclaration> {
    val properties = classDeclaration.primaryConstructor?.parameters ?: return emptyList()
    val dependentClasses = mutableListOf<KSClassDeclaration>()

    properties.forEach { param ->
        val paramType = param.type.resolve()
        // Unwrap nullable types to get the actual type declaration
        val nonNullType = paramType.makeNotNullable()
        val typeDeclaration = nonNullType.declaration as? KSClassDeclaration

        if (typeDeclaration != null &&
            typeDeclaration.modifiers.contains(Modifier.DATA)
        ) {
            val hasCompanionObject = typeDeclaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .any { it.isCompanionObject }

            if (hasCompanionObject) {
                dependentClasses.add(typeDeclaration)
                logger.info("Found dependent data class: ${typeDeclaration.qualifiedName?.asString()}")
            }
        }
    }

    return dependentClasses
}

private fun generateCombinedFieldForClass(
    classDeclaration: KSClassDeclaration,
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
) {
    // Validate it's a data class
    if (!classDeclaration.modifiers.contains(Modifier.DATA)) {
        logger.error("@GenerateCombinedField can only be applied to data classes", classDeclaration)
        return
    }

    // Find companion object
    val companionObject = classDeclaration.declarations
        .filterIsInstance<KSClassDeclaration>()
        .firstOrNull { it.isCompanionObject }

    if (companionObject == null) {
        logger.error("@GenerateCombinedField requires a companion object", classDeclaration)
        return
    }

    // Get primary constructor parameters
    val properties = classDeclaration.primaryConstructor?.parameters ?: run {
        logger.error("@GenerateCombinedField requires a primary constructor", classDeclaration)
        return
    }

    if (properties.isEmpty()) {
        logger.error("@GenerateCombinedField requires at least one property", classDeclaration)
        return
    }

    if (properties.size > 10) {
        logger.error("@GenerateCombinedField supports up to 10 properties, but found ${properties.size}", classDeclaration)
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
        writer.write(generateCombinedFieldCode(className, qualifiedClassName, packageName, properties, logger))
    }
}

private fun generateCombinedFieldCode(
    className: String,
    qualifiedClassName: String,
    packageName: String,
    properties: List<com.google.devtools.ksp.symbol.KSValueParameter>,
    logger: KSPLogger,
): String {
    val fieldCount = properties.size
    val imports = mutableSetOf<String>()

    imports.add("me.tbsten.compose.preview.lab.field.MutablePreviewLabField")
    imports.add("me.tbsten.compose.preview.lab.field.CombinedField$fieldCount")
    imports.add("me.tbsten.compose.preview.lab.field.splitedOf")

    // Build field creators
    val fieldCreators = properties.mapIndexed { index, param ->
        val paramName = param.name?.asString() ?: "param$index"
        val paramType = param.type.resolve()
        val fieldCreator = generateFieldCreator(paramName, paramType, imports, logger)
        "    field${index + 1} = $fieldCreator"
    }

    // Build combine lambda parameters and call
    val combineParams = properties.mapIndexed { index, param ->
        param.name?.asString() ?: "param$index"
    }.joinToString(", ")

    val combineCall = properties.joinToString(", ") { param ->
        val paramName = param.name?.asString() ?: ""
        "$paramName = $paramName"
    }

    // Build split call
    val splitParams = properties.joinToString(", ") { param ->
        "it.${param.name?.asString()}"
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
        appendLine(" * Creates a MutablePreviewLabField<$className> with CombinedField$fieldCount.")
        appendLine(" */")
        appendLine("fun $className.Companion.field(")
        appendLine("    label: String,")
        appendLine("    initialValue: $className,")
        appendLine("): MutablePreviewLabField<$className> = CombinedField$fieldCount(")
        appendLine("    label = label,")
        fieldCreators.forEach { appendLine(it + ",") }
        appendLine("    combine = { $combineParams ->")
        appendLine("        $className($combineCall)")
        appendLine("    },")
        appendLine("    split = { splitedOf($splitParams) },")
        appendLine(")")
    }

    return code
}

private fun generateFieldCreator(
    paramName: String,
    paramType: KSType,
    imports: MutableSet<String>,
    logger: KSPLogger,
): String {
    // Check if the type is nullable
    val isNullable = paramType.isMarkedNullable
    val nonNullType = if (isNullable) {
        // Get the non-null version of the type
        paramType.makeNotNullable()
    } else {
        paramType
    }

    val typeName = nonNullType.declaration.simpleName.asString()
    val qualifiedTypeName = nonNullType.declaration.qualifiedName?.asString()
    val typeDeclaration = nonNullType.declaration as? KSClassDeclaration

    // Generate the base field creator
    val baseFieldCreator = generateBaseFieldCreator(
        paramName,
        nonNullType,
        typeName,
        qualifiedTypeName,
        typeDeclaration,
        imports,
        logger,
    )

    // Wrap with nullable() if needed
    return if (isNullable) {
        imports.add("me.tbsten.compose.preview.lab.field.nullable")
        // For nullable fields, we need to adjust the baseFieldCreator to handle null values
        // by providing a default value when unwrapping value classes or accessing properties
        val adjustedBaseFieldCreator = adjustBaseFieldCreatorForNullable(
            baseFieldCreator,
            paramName,
            nonNullType,
            typeDeclaration,
        )
        "$adjustedBaseFieldCreator.nullable(initialValue = initialValue.$paramName)"
    } else {
        baseFieldCreator
    }
}

private fun adjustBaseFieldCreatorForNullable(
    baseFieldCreator: String,
    paramName: String,
    nonNullType: KSType,
    typeDeclaration: KSClassDeclaration?,
): String {
    // If it's a Transform Field for value class, we need to use safe navigation
    if (baseFieldCreator.contains("TransformField")) {
        // Replace `initialValue.$paramName.property` with `initialValue.$paramName?.property ?: defaultValue`
        // We need to find the default value for the underlying type
        val underlyingProperty = typeDeclaration?.getAllProperties()?.firstOrNull()
        if (underlyingProperty != null) {
            val underlyingType = underlyingProperty.type.resolve()
            val defaultValue = getDefaultValueForType(underlyingType)
            val propertyName = underlyingProperty.simpleName.asString()
            return baseFieldCreator.replace(
                "initialValue.$paramName.$propertyName",
                "initialValue.$paramName?.$propertyName ?: $defaultValue",
            )
        }
    }
    // For other nullable fields, just use default values
    return baseFieldCreator.replace(
        "initialValue.$paramName",
        "initialValue.$paramName ?: ${getDefaultValueForType(nonNullType)}",
    )
}

private fun getDefaultValueForType(type: KSType): String {
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

private fun generateBaseFieldCreator(
    paramName: String,
    paramType: KSType,
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
        return "EnumField<$typeName>(label = \"$paramName\", initialValue = initialValue.$paramName)"
    }

    // 2. Check for Value class (inline value class)
    // Value classes are marked with @JvmInline annotation in Kotlin
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
        logger.info("Found value class: $qualifiedTypeName")
        // Value classes have a single property that holds the actual value
        val underlyingProperty = typeDeclaration.getAllProperties().firstOrNull()
        if (underlyingProperty != null) {
            val underlyingType = underlyingProperty.type.resolve()
            val propertyName = underlyingProperty.simpleName.asString()

            // Generate the field creator for the underlying type, but use the unwrapped initial value
            val underlyingFieldCreatorPattern = generateFieldCreatorPattern(
                paramName,
                underlyingType,
                imports,
                logger,
            )

            // We need to unwrap the value class to get its underlying value, then wrap it back
            imports.add("me.tbsten.compose.preview.lab.field.TransformField")
            val qualifiedName = typeDeclaration.qualifiedName?.asString() ?: typeName
            imports.add(qualifiedName)

            // Insert initialValue into the underlying field creator pattern
            val baseFieldWithInitialValue = if (underlyingFieldCreatorPattern.trim().endsWith(")")) {
                // Insert before the last ')'
                val idx = underlyingFieldCreatorPattern.lastIndexOf(')')
                underlyingFieldCreatorPattern.substring(0, idx) +
                    ", initialValue = initialValue.$paramName.$propertyName" +
                    underlyingFieldCreatorPattern.substring(idx)
            } else {
                // Fallback: just append
                "$underlyingFieldCreatorPattern, initialValue = initialValue.$paramName.$propertyName"
            }

            return "TransformField(" +
                "label = \"$paramName\", " +
                "baseField = $baseFieldWithInitialValue, " +
                "transform = { $typeName(it) }, " +
                "reverse = { it.$propertyName }" +
                ")"
        }
    }

    // 3. Check if the type has @GenerateCombinedField annotation
    val isGeneratedField = paramType.declaration.annotations.any {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == GenerateCombinedFieldAnnotation
    }

    if (isGeneratedField) {
        // Use the generated field function
        return "$typeName.field(label = \"$paramName\", initialValue = initialValue.$paramName)"
    }

    // 4. Check if it's a data class with companion object (potential for recursive field generation)
    if (typeDeclaration != null && typeDeclaration.modifiers.contains(Modifier.DATA)) {
        val hasCompanionObject = typeDeclaration.declarations
            .filterIsInstance<KSClassDeclaration>()
            .any { it.isCompanionObject }

        if (hasCompanionObject) {
            // This data class has a companion object, so it can potentially have a field() function
            // Try to use it recursively
            logger.info("Found data class with companion object: $qualifiedTypeName, will try to use field() function")
            return "$typeName.field(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
    }

    // 5. Try to map primitive types to field types
    val primitiveFieldType = getPrimitiveFieldType(qualifiedTypeName, imports)
    val fieldType = if (primitiveFieldType != null) {
        primitiveFieldType
    } else {
        // Check if the type has generic parameters (e.g., List<String>, Map<String, Int>)
        if (paramType.arguments.isNotEmpty()) {
            logger.error(
                "Generic types are not supported: $qualifiedTypeName for property $paramName. " +
                    "Please use a non-generic type or create a custom wrapper type.",
                null
            )
            imports.add("me.tbsten.compose.preview.lab.field.StringField")
            return "StringField(label = \"$paramName\", initialValue = \"\")"
        }

        // 6. Fallback: check if there's a known field type for this type
        val potentialFieldType = findKnownFieldType(qualifiedTypeName, paramName, imports)
        if (potentialFieldType != null) {
            return potentialFieldType
        }

        logger.warn("Unsupported type $qualifiedTypeName for property $paramName. Falling back to StringField.")
        imports.add("me.tbsten.compose.preview.lab.field.StringField")
        "StringField"
    }

    return "$fieldType(label = \"$paramName\", initialValue = initialValue.$paramName)"
}

/**
 * Maps a qualified type name to its corresponding field type.
 * Returns null if the type is not a recognized primitive type.
 */
private fun getPrimitiveFieldType(qualifiedTypeName: String?, imports: MutableSet<String>): String? = when (qualifiedTypeName) {
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

/**
 * Generates just the field constructor pattern without the initialValue parameter.
 * This is used for value classes where we need to provide a custom initialValue.
 */
private fun generateFieldCreatorPattern(
    paramName: String,
    paramType: KSType,
    imports: MutableSet<String>,
    logger: KSPLogger,
): String {
    val qualifiedTypeName = paramType.declaration.qualifiedName?.asString()

    // Map primitive types to field types
    val fieldType = getPrimitiveFieldType(qualifiedTypeName, imports) ?: run {
        logger.warn("Unsupported underlying type $qualifiedTypeName for value class. Falling back to StringField.")
        imports.add("me.tbsten.compose.preview.lab.field.StringField")
        "StringField"
    }

    return "$fieldType(label = \"$paramName\""
}

/**
 * Searches for known field types in the compose-preview-lab library
 */
private fun findKnownFieldType(qualifiedTypeName: String?, paramName: String, imports: MutableSet<String>,): String? {
    if (qualifiedTypeName == null) return null

    // Map known types to their corresponding field types
    return when (qualifiedTypeName) {
        "androidx.compose.ui.unit.Dp" -> {
            imports.add("me.tbsten.compose.preview.lab.field.DpField")
            "DpField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        "androidx.compose.ui.unit.DpOffset" -> {
            imports.add("me.tbsten.compose.preview.lab.field.DpOffsetField")
            "DpOffsetField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        "androidx.compose.ui.unit.DpSize" -> {
            imports.add("me.tbsten.compose.preview.lab.field.DpSizeField")
            "DpSizeField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        "androidx.compose.ui.unit.TextUnit" -> {
            imports.add("me.tbsten.compose.preview.lab.field.SpField")
            "SpField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        "androidx.compose.ui.graphics.Color" -> {
            imports.add("me.tbsten.compose.preview.lab.field.ColorField")
            "ColorField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        "androidx.compose.ui.Modifier" -> {
            imports.add("me.tbsten.compose.preview.lab.field.ModifierField")
            "ModifierField(label = \"$paramName\", initialValue = initialValue.$paramName)"
        }
        else -> null
    }
}

/**
 * Generates a PolymorphicField extension function for a sealed interface or sealed class.
 */
private fun generatePolymorphicFieldForSealedClass(
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
            )
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
                    subclass,
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
    subclass: KSClassDeclaration,
    subclassName: String,
    sealedClassName: String,
    properties: List<com.google.devtools.ksp.symbol.KSValueParameter>,
    imports: MutableSet<String>,
    logger: KSPLogger,
): String {
    val fieldCount = properties.size

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
        nonNullType,
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
    paramType: KSType,
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
