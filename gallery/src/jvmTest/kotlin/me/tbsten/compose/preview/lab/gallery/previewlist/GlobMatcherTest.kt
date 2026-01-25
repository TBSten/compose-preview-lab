package me.tbsten.compose.preview.lab.gallery.previewlist

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GlobMatcherTest :
    StringSpec({

        "isGlobPattern should detect glob patterns" {
            isGlobPattern("*.kt") shouldBe true
            isGlobPattern("**/*.kt") shouldBe true
            isGlobPattern("file?.kt") shouldBe true
            isGlobPattern("src/main/kotlin/MyFile.kt") shouldBe false
        }

        "matchesGlob with exact path should work" {
            matchesGlob("src/main/kotlin/MyFile.kt", "src/main/kotlin/MyFile.kt") shouldBe true
            matchesGlob("src/main/kotlin/MyFile.kt", "src/main/kotlin/OtherFile.kt") shouldBe false
        }

        "matchesGlob with * should match single segment" {
            matchesGlob("src/main/kotlin/MyFile.kt", "src/main/kotlin/*.kt") shouldBe true
            matchesGlob("src/main/kotlin/MyFile.kt", "src/main/kotlin/*.java") shouldBe false
            matchesGlob("src/main/kotlin/sub/MyFile.kt", "src/main/kotlin/*.kt") shouldBe false
        }

        "matchesGlob with ** should match multiple segments" {
            matchesGlob("src/main/kotlin/MyFile.kt", "**/*.kt") shouldBe true
            matchesGlob("src/main/kotlin/sub/MyFile.kt", "**/*.kt") shouldBe true
            matchesGlob("MyFile.kt", "**/*.kt") shouldBe true
            matchesGlob("src/main/kotlin/MyFile.kt", "src/**/*.kt") shouldBe true
            matchesGlob("src/MyFile.kt", "src/**/*.kt") shouldBe true
        }

        "matchesGlob with ? should match single character" {
            matchesGlob("src/main/kotlin/MyFile1.kt", "src/main/kotlin/MyFile?.kt") shouldBe true
            matchesGlob("src/main/kotlin/MyFile12.kt", "src/main/kotlin/MyFile?.kt") shouldBe false
        }

        "matchesGlob should handle path/**/*.ext pattern" {
            matchesGlob("app/src/main/kotlin/Button.kt", "app/**/*.kt") shouldBe true
            matchesGlob("app/src/main/kotlin/sub/Button.kt", "app/**/*.kt") shouldBe true
            matchesGlob("lib/src/main/kotlin/Button.kt", "app/**/*.kt") shouldBe false
        }

        "matchesGlob should handle **/path pattern" {
            matchesGlob("src/main/kotlin/MyFile.kt", "**/kotlin/*.kt") shouldBe true
            matchesGlob("kotlin/MyFile.kt", "**/kotlin/*.kt") shouldBe true
        }

        "matchesGlob should normalize backslashes" {
            matchesGlob("src\\main\\kotlin\\MyFile.kt", "src/main/kotlin/*.kt") shouldBe true
            matchesGlob("src/main/kotlin/MyFile.kt", "src\\main\\kotlin\\*.kt") shouldBe true
        }

        "matchesGlob should escape regex special characters" {
            matchesGlob("src/main/kotlin/My[File].kt", "src/main/kotlin/My[File].kt") shouldBe true
            matchesGlob("src/main/kotlin/My.File.kt", "src/main/kotlin/My.File.kt") shouldBe true
        }
    })
