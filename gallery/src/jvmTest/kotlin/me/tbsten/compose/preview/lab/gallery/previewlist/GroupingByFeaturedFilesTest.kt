package me.tbsten.compose.preview.lab.gallery.previewlist

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.CollectedPreview
import me.tbsten.compose.preview.lab.PreviewLabPreview

class GroupingByFeaturedFilesTest :
    StringSpec({

        "groupingByFeaturedFiles with exact paths" {
            val previews = listOf(
                previewWithPath("app/src/main/kotlin/ButtonPreview.kt"),
                previewWithPath("app/src/main/kotlin/TextPreview.kt"),
                previewWithPath("lib/src/main/kotlin/IconPreview.kt"),
            )
            val featuredFiles = mapOf(
                "buttons" to listOf("app/src/main/kotlin/ButtonPreview.kt"),
                "all-app" to listOf(
                    "app/src/main/kotlin/ButtonPreview.kt",
                    "app/src/main/kotlin/TextPreview.kt",
                ),
            )

            val result = previews.groupingByFeaturedFiles(featuredFiles)

            result["buttons"]?.size shouldBe 1
            result["buttons"]?.first()?.filePath shouldBe "app/src/main/kotlin/ButtonPreview.kt"
            result["all-app"]?.size shouldBe 2
        }

        "groupingByFeaturedFiles with glob patterns" {
            val previews = listOf(
                previewWithPath("app/src/main/kotlin/ButtonPreview.kt"),
                previewWithPath("app/src/main/kotlin/TextPreview.kt"),
                previewWithPath("lib/src/main/kotlin/IconPreview.kt"),
            )
            val featuredFiles = mapOf(
                "all-kotlin" to listOf("**/*.kt"),
                "app-only" to listOf("app/**/*.kt"),
                "lib-only" to listOf("lib/**/*.kt"),
            )

            val result = previews.groupingByFeaturedFiles(featuredFiles)

            result["all-kotlin"]?.size shouldBe 3
            result["app-only"]?.size shouldBe 2
            result["lib-only"]?.size shouldBe 1
        }

        "groupingByFeaturedFiles with mixed patterns and paths" {
            val previews = listOf(
                previewWithPath("app/src/main/kotlin/ui/ButtonPreview.kt"),
                previewWithPath("app/src/main/kotlin/ui/TextPreview.kt"),
                previewWithPath("app/src/main/kotlin/data/DataPreview.kt"),
            )
            val featuredFiles = mapOf(
                "ui-components" to listOf("app/src/main/kotlin/ui/*.kt"),
                "specific" to listOf("app/src/main/kotlin/data/DataPreview.kt"),
            )

            val result = previews.groupingByFeaturedFiles(featuredFiles)

            result["ui-components"]?.size shouldBe 2
            result["specific"]?.size shouldBe 1
        }

        "groupingByFeaturedFiles with null filePath should not match" {
            val previews = listOf(
                previewWithPath(null),
                previewWithPath("app/src/main/kotlin/ButtonPreview.kt"),
            )
            val featuredFiles = mapOf(
                "all" to listOf("**/*.kt"),
            )

            val result = previews.groupingByFeaturedFiles(featuredFiles)

            result["all"]?.size shouldBe 1
        }

        "groupingByFeaturedFiles with empty patterns" {
            val previews = listOf(
                previewWithPath("app/src/main/kotlin/ButtonPreview.kt"),
            )
            val featuredFiles = mapOf(
                "empty" to emptyList<String>(),
            )

            val result = previews.groupingByFeaturedFiles(featuredFiles)

            result["empty"]?.size shouldBe 0
        }
    })

private var idCounter = 0

private fun previewWithPath(filePath: String?): PreviewLabPreview {
    val id = "preview-${idCounter++}"
    return CollectedPreview(
        id = id,
        displayName = id,
        filePath = filePath,
    ) { }
}
