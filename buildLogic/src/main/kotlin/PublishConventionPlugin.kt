import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomDeveloperSpec
import org.gradle.api.publish.maven.MavenPomLicenseSpec
import util.libs
import util.mavenPublishing
import util.plugin

private const val GithubUserName = "TBSten"
private const val AuthorName = GithubUserName
private const val GithubRepoName = "compose-preview-lab"

class PublishConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply(libs.plugin("vanniktechMavenPublish").pluginId)

        val publishConvention =
            target.extensions.create("publishConvention", PublishConventionExtension::class.java)
                .apply {
                    groupId = rootProject.group.toString()
                    artifactId = rootProject.name
                    version = rootProject.version.toString()
                }

        configurePublish(publishConvention)
        configureDokka(publishConvention)
    }
}

private fun Project.configurePublish(publishConvention: PublishConventionExtension) {
    afterEvaluate {
        mavenPublishing {
            publishToMavenCentral()

            if (!(gradle.startParameter.taskNames.contains("publishToMavenLocal"))) {
                signAllPublications()
            }

            coordinates(
                groupId = publishConvention.groupId ?: group.toString(),
                artifactId = publishConvention.artifactId,
                version = publishConvention.version ?: version.toString(),
            )

            pom {
                name.set(publishConvention.artifactId)
                description.set(publishConvention.description)
                inceptionYear.set("2025")
                url.set(publishConvention.url)

                licenses {
                    apacheLicense20()
                }

                developers {
                    developerTbsten()
                }

                scmGithub()
            }
        }
    }
}

open class PublishConventionExtension {
    var groupId: String? = null
    var artifactId: String? = null
    var artifactName: String? = artifactId
        get() = field ?: artifactId

    var version: String? = null
    lateinit var description: String
    var url: String = "https://github.com/$GithubUserName/$GithubRepoName"
}

private fun MavenPomLicenseSpec.apacheLicense20() {
    license {
        name.set("The Apache License, Version 2.0")
        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
    }
}

private fun MavenPomDeveloperSpec.developerTbsten() {
    developer {
        id.set(GithubUserName)
        name.set(AuthorName)
        url.set("https://github.com/$GithubUserName")
    }
}

private fun MavenPom.scmGithub() {
    scm {
        url.set("https://github.com/$GithubUserName/$GithubRepoName")
        connection.set("scm:git:git://github.com/$GithubUserName/")
        developerConnection.set("scm:git:ssh://git@github.com/$GithubUserName/$GithubRepoName.git")
    }
}
