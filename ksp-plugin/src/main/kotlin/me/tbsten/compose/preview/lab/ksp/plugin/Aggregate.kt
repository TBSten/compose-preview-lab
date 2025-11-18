package me.tbsten.compose.preview.lab.ksp.plugin

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import me.tbsten.compose.preview.lab.AggregateToAll
import me.tbsten.compose.preview.lab.InternalComposePreviewLabApi

internal fun prepareModuleForPreviewAllAggregate(codeGenerator: CodeGenerator, previewsListPackage: String) {
    codeGenerator.createNewFile(
        dependencies = Dependencies.ALL_FILES,
        packageName = "me.tbsten.compose.preview.lab.generated",
        fileName = "__${previewsListPackage}__previewsForAggregateAll",
    ).bufferedWriter().use {
        val forAggregatePreviewAllPropertyName =
            "`__${previewsListPackage}__previewsForAggregateAll`"

        it.appendLine("package me.tbsten.compose.preview.lab.generated")
        it.appendLine()
        it.appendLine("import ${InternalComposePreviewLabApi::class.qualifiedName}")
        it.appendLine("import ${AggregateToAll::class.qualifiedName}")
        it.appendLine("import me.tbsten.compose.preview.lab.CollectedPreview")
        it.appendLine()
        it.appendLine("@Suppress(\"RemoveRedundantBackticks\", \"ObjectPropertyName\", \"unused\")")
        it.appendLine("@${InternalComposePreviewLabApi::class.simpleName}")
        it.appendLine("@${AggregateToAll::class.simpleName}")
        it.appendLine("val $forAggregatePreviewAllPropertyName: List<CollectedPreview> = $previewsListPackage.Previews")
    }
}

@OptIn(KspExperimental::class)
internal fun generatePreviewAll(resolver: Resolver, codeGenerator: CodeGenerator, previewsListPackage: String) {
    val previewsProperty =
        sequenceOf("$previewsListPackage.Previews") +
            resolver
                .getDeclarationsFromPackage("me.tbsten.compose.preview.lab.generated")
                .filterIsInstance<KSPropertyDeclaration>()
                .filter { it.isAnnotationPresent(AggregateToAll::class) }
                .map { "${it.packageName.asString()}.`${it.simpleName.asString()}`" }

    codeGenerator.createNewFile(
        dependencies = Dependencies.ALL_FILES,
        packageName = previewsListPackage,
        fileName = "PreviewsAll",
    ).bufferedWriter().use {
        it.appendLine("package $previewsListPackage")
        it.appendLine("import me.tbsten.compose.preview.lab.CollectedPreview")
        it.appendLine()
        it.appendLine("@OptIn(${InternalComposePreviewLabApi::class.qualifiedName}::class)")
        it.appendLine("object PreviewsAll : List<CollectedPreview> by (")
        it.appendLine("    (")
        it.appendLine(
            previewsProperty
                .map { "        $it" }
                .joinToString(" +\n"),
        )
        it.appendLine("    )")
        it.appendLine(")")
    }
}
