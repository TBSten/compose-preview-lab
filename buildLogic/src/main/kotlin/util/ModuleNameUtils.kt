package util

/**
 * Converts a kebab-case module name to various formats.
 */
object ModuleNameUtils {
    private const val NAMESPACE_PREFIX = "me.tbsten.compose.preview.lab"
    private const val JS_MODULE_PREFIX = "compose-preview-lab"
    private const val IOS_FRAMEWORK_PREFIX = "ComposePreviewLab"

    /**
     * Converts moduleBaseName to Android namespace.
     * e.g., "extension-kotlinx-datetime" -> "me.tbsten.compose.preview.lab.extension.kotlinx.datetime"
     */
    fun toAndroidNamespace(moduleBaseName: String): String {
        val suffix = moduleBaseName.replace("-", ".")
        return "$NAMESPACE_PREFIX.$suffix"
    }

    /**
     * Converts moduleBaseName to JS output module name.
     * e.g., "extension-kotlinx-datetime" -> "compose-preview-lab-extension-kotlinx-datetime"
     */
    fun toJsOutputModuleName(moduleBaseName: String): String {
        return "$JS_MODULE_PREFIX-$moduleBaseName"
    }

    /**
     * Converts moduleBaseName to iOS framework base name.
     * e.g., "extension-kotlinx-datetime" -> "ComposePreviewLabExtensionKotlinxDatetime"
     */
    fun toIosFrameworkBaseName(moduleBaseName: String): String {
        val pascalCase = moduleBaseName
            .split("-")
            .joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }
        return "$IOS_FRAMEWORK_PREFIX$pascalCase"
    }
}
