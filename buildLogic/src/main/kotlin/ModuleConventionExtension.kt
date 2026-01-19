import org.gradle.api.provider.Property

/**
 * Common extension interface for convention plugins that require a module base name.
 */
interface ModuleConventionExtension {
    /**
     * Base name for the module (e.g., "annotation", "core", "extension-kotlinx-datetime").
     * This is used to generate:
     * - androidNamespace: "me.tbsten.compose.preview.lab." + moduleBaseName.replace("-", ".")
     * - jsOutputModuleName: "compose-preview-lab-" + moduleBaseName
     * - iosFrameworkBaseName: "ComposePreviewLab" + toPascalCase(moduleBaseName)
     */
    val moduleBaseName: Property<String>
}
