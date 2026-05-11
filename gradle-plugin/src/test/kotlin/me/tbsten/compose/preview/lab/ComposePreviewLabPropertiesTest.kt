package me.tbsten.compose.preview.lab

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testfixtures.ProjectBuilder

/**
 * Unit tests for the `gradle.properties` plumbing in [ComposePreviewLabProperties] and
 * friends. Full integration scenarios (DSL > properties > default precedence, `-P` override,
 * compiler-plugin wiring) are covered by the separate integrationTest project.
 */
class ComposePreviewLabPropertiesTest :
    FunSpec({

        context("parseBoolProp") {
            test("'true' / 'false' parse to the expected Boolean regardless of case") {
                parseBoolProp("true", default = false) shouldBe true
                parseBoolProp("TRUE", default = false) shouldBe true
                parseBoolProp("False", default = true) shouldBe false
            }

            test("blank / null values fall back to the default") {
                parseBoolProp(null, default = true) shouldBe true
                parseBoolProp("", default = false) shouldBe false
                parseBoolProp("   ", default = true) shouldBe true
            }

            test("unrecognized strings fall back to the default") {
                parseBoolProp("yes", default = false) shouldBe false
                parseBoolProp("1", default = true) shouldBe true
                parseBoolProp("garbage", default = false) shouldBe false
            }
        }

        context("AllKeys") {
            test("contains every recognized prefixed key") {
                ComposePreviewLabProperties.AllKeys shouldBe setOf(
                    "composePreviewLab.generatePackage",
                    "composePreviewLab.projectRootPath",
                    "composePreviewLab.generateFeaturedFiles",
                    "composePreviewLab.collectPreviews.enabled",
                    "composePreviewLab.collectPreviews.defaultCollectScope",
                )
            }

            test("every key starts with the shared prefix (single source of truth check)") {
                ComposePreviewLabProperties.AllKeys.forEach { key ->
                    key.startsWith(ComposePreviewLabProperties.Prefix) shouldBe true
                }
            }
        }

        context("unknownComposePreviewLabPropertyKeys") {
            test("returns empty when every prefixed key is known") {
                val keys = listOf(
                    "composePreviewLab.generatePackage",
                    "composePreviewLab.collectPreviews.enabled",
                    "org.gradle.parallel",
                    "unrelated.foo",
                )
                unknownComposePreviewLabPropertyKeys(keys) shouldContainExactly emptyList()
            }

            test("returns unknown prefixed keys in sorted order, ignoring keys without the prefix") {
                val keys = listOf(
                    "composePreviewLab.generatePackge", // typo
                    "composePreviewLab.collectPreviews.enableed", // typo
                    "composePreviewLab.generatePackage", // known
                    "unrelated.composePreviewLab.something", // does not start with prefix
                    "org.gradle.parallel",
                )
                unknownComposePreviewLabPropertyKeys(keys) shouldContainExactly listOf(
                    "composePreviewLab.collectPreviews.enableed",
                    "composePreviewLab.generatePackge",
                )
            }
        }

        context("boolProp / stringProp resolution sources") {
            // Regression guard for the task-024 bug: a subproject `gradle.properties` entry like
            // `composePreviewLab.generateFeaturedFiles=true` is silently ignored because
            // `project.providers.gradleProperty(...)` only sees root `gradle.properties` /
            // `GRADLE_USER_HOME` / `-P` / env / system, NOT subproject-local files. The fallback
            // through `project.findProperty(...)` covers that gap.
            test("boolProp falls back to project.findProperty for subproject-local property values") {
                val project = ProjectBuilder.builder().build()
                project.extensions.extraProperties["composePreviewLab.generateFeaturedFiles"] = "true"
                val provider = boolProp(
                    project = project,
                    key = "composePreviewLab.generateFeaturedFiles",
                    default = false,
                )
                provider.get() shouldBe true
            }

            test("boolProp returns default when neither providers.gradleProperty nor findProperty has the key") {
                val project = ProjectBuilder.builder().build()
                val provider = boolProp(
                    project = project,
                    key = "composePreviewLab.generateFeaturedFiles",
                    default = false,
                )
                provider.get() shouldBe false
            }

            test("stringProp falls back to project.findProperty for subproject-local property values") {
                val project = ProjectBuilder.builder().build()
                project.extensions.extraProperties["composePreviewLab.generatePackage"] = "fromExtra"
                val provider = stringProp(
                    project = project,
                    key = "composePreviewLab.generatePackage",
                    default = "fromDefault",
                )
                provider.get() shouldBe "fromExtra"
            }

            test("stringProp falls back to default when neither source has the key") {
                val project = ProjectBuilder.builder().build()
                val provider = stringProp(
                    project = project,
                    key = "composePreviewLab.generatePackage",
                    default = "fromDefault",
                )
                provider.get() shouldBe "fromDefault"
            }

            test("stringProp treats blank subproject value as missing and falls back to default") {
                val project = ProjectBuilder.builder().build()
                project.extensions.extraProperties["composePreviewLab.generatePackage"] = "   "
                val provider = stringProp(
                    project = project,
                    key = "composePreviewLab.generatePackage",
                    default = "fromDefault",
                )
                provider.get() shouldBe "fromDefault"
            }
        }

        context("unknownComposePreviewLabPropertyWarning") {
            test("returns null when there are no unknown keys (no warning to emit)") {
                unknownComposePreviewLabPropertyWarning(emptyList()).shouldBeNull()
            }

            test("renders a message including every unknown key and the full known-key catalog") {
                val message = unknownComposePreviewLabPropertyWarning(
                    listOf(
                        "composePreviewLab.generatePackge",
                        "composePreviewLab.collectPreviews.enableed",
                    ),
                )
                message.shouldNotBeNull()
                message shouldContain "composePreviewLab.generatePackge"
                message shouldContain "composePreviewLab.collectPreviews.enableed"
                // Catalog of known keys must be advertised so users can self-correct typos.
                message shouldContain "composePreviewLab.generatePackage"
                message shouldContain "composePreviewLab.collectPreviews.defaultCollectScope"
                // Sanity: nothing about the message implies missing prefix logic.
                message shouldNotContain "unrelated."
            }
        }
    })
