import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class CmpConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val extension = extensions.create<CmpConventionExtension>("cmpConvention")
        configureCmp(moduleBaseNameProvider = { extension.moduleBaseName.get() })
    }
}
