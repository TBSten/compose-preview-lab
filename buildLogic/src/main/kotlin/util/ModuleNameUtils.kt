package util

import org.gradle.api.GradleException

/**
 * Converts a kebab-case module name to various formats.
 */
object ModuleNameUtils {
    private const val NAMESPACE_PREFIX = "me.tbsten.compose.preview.lab"
    private const val JS_MODULE_PREFIX = "compose-preview-lab"
    private const val IOS_FRAMEWORK_PREFIX = "ComposePreviewLab"

    /**
     * Valid pattern for moduleBaseName: kebab-case starting with lowercase letter.
     * Examples: "core", "annotation", "extension-kotlinx-datetime"
     */
    private val VALID_MODULE_NAME_PATTERN = Regex("^[a-z][a-z0-9]*(-[a-z0-9]+)*$")

    /**
     * Validates that the moduleBaseName is properly formatted.
     * @throws GradleException if the moduleBaseName is invalid
     */
    fun validate(moduleBaseName: String) {
        if (moduleBaseName.isBlank()) {
            throw GradleException(
                """
                |[Convention Plugin] moduleBaseName must not be blank.
                |
                |Please configure it in your build.gradle.kts:
                |
                |    kmpConvention {  // or cmpConvention
                |        moduleBaseName = "your-module-name"
                |    }
                |
                |Example values: "core", "annotation", "extension-kotlinx-datetime"
                """.trimMargin()
            )
        }
        if (!moduleBaseName.matches(VALID_MODULE_NAME_PATTERN)) {
            throw GradleException(
                """
                |[Convention Plugin] Invalid moduleBaseName: "$moduleBaseName"
                |
                |moduleBaseName must be kebab-case (lowercase letters, numbers, and hyphens).
                |It must start with a lowercase letter and cannot have consecutive hyphens.
                |
                |Valid examples: "core", "annotation", "extension-kotlinx-datetime"
                |Invalid examples: "Core", "my_module", "-invalid", "invalid-"
                """.trimMargin()
            )
        }
    }

    /**
     * Converts moduleBaseName to Android namespace.
     * e.g., "extension-kotlinx-datetime" -> "me.tbsten.compose.preview.lab.extension.kotlinx.datetime"
     */
    fun toAndroidNamespace(moduleBaseName: String): String {
        validate(moduleBaseName)
        val suffix = moduleBaseName.replace("-", ".")
        return "$NAMESPACE_PREFIX.$suffix"
    }

    /**
     * Converts moduleBaseName to JS output module name.
     * e.g., "extension-kotlinx-datetime" -> "compose-preview-lab-extension-kotlinx-datetime"
     */
    fun toJsOutputModuleName(moduleBaseName: String): String {
        validate(moduleBaseName)
        return "$JS_MODULE_PREFIX-$moduleBaseName"
    }

    /**
     * Converts moduleBaseName to iOS framework base name.
     * e.g., "extension-kotlinx-datetime" -> "ComposePreviewLabExtensionKotlinxDatetime"
     */
    fun toIosFrameworkBaseName(moduleBaseName: String): String {
        validate(moduleBaseName)
        val pascalCase = moduleBaseName
            .split("-")
            .joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }
        return "$IOS_FRAMEWORK_PREFIX$pascalCase"
    }
}
