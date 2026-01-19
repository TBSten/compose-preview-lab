import org.gradle.api.provider.Property

interface CmpConventionExtension {
    /**
     * Base name for the module (e.g., "core", "field", "extension-kotlinx-datetime").
     * This is used to generate:
     * - androidNamespace: "me.tbsten.compose.preview.lab." + moduleBaseName.replace("-", ".")
     * - jsOutputModuleName: "compose-preview-lab-" + moduleBaseName
     * - iosFrameworkBaseName: "ComposePreviewLab" + toPascalCase(moduleBaseName)
     */
    val moduleBaseName: Property<String>
}
