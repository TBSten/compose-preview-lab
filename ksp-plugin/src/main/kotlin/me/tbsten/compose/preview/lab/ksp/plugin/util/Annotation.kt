package me.tbsten.compose.preview.lab.ksp.plugin.util

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

internal fun KSFunctionDeclaration.findAnnotation(annotation: String,): KSAnnotation? = annotations.firstOrNull {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == annotation
}

internal inline fun <reified Value> KSAnnotation.findArg(name: String) = this
    .arguments
    .find { it.name?.asString() == name }
    ?.value as? Value
