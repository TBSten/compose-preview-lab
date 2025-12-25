package me.tbsten.compose.preview.lab.component

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import me.tbsten.compose.preview.lab.CollectedPreview
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.gallery.previewlist.PreviewTreeNode
import me.tbsten.compose.preview.lab.gallery.previewlist.collapse
import me.tbsten.compose.preview.lab.gallery.previewlist.toTree

class PreviewListTreeTest :
    StringSpec({

        "testToTree" {
            val previews = listOf(
                previewForTest("a.b.c"),
                previewForTest("a.b.d.x"),
                previewForTest("a.b.e"),
                previewForTest("a.b"),
                previewForTest("e.f"),
                previewForTest("e"),
                previewForTest("e.a.b"),
            )

            val tree = previews.toTree()
            // a
            val a = tree.getOrNull(0)
            a.shouldBeInstanceOf<PreviewTreeNode.Group>()
            a.groupName shouldBe "a"
            // a.b
            val aB = a.children.getOrNull(0)
            aB.shouldBeInstanceOf<PreviewTreeNode.Group>()
            aB.groupName shouldBe "b"
            // a.b.c
            val aBC = aB.children.getOrNull(0)
            aBC.shouldBeInstanceOf<PreviewTreeNode.Preview>()
            aBC.preview shouldBe previews[0]
            // a.b.d
            val aBD = aB.children.getOrNull(1)
            aBD.shouldBeInstanceOf<PreviewTreeNode.Group>()
            aBD.groupName shouldBe "d"
            // a.b.d.x
            val aBDX = aBD.children.getOrNull(0)
            aBDX.shouldBeInstanceOf<PreviewTreeNode.Preview>()
            aBDX.preview shouldBe previews[1]
            // a.b.e
            val aBE = aB.children.getOrNull(2)
            aBE.shouldBeInstanceOf<PreviewTreeNode.Preview>()
            aBE.preview shouldBe previews[2]
            // a.b
            val aB2 = a.children.getOrNull(1)
            aB2.shouldBeInstanceOf<PreviewTreeNode.Preview>()
            aB2.preview shouldBe previews[3]
            // e
            val e = tree.getOrNull(1)
            e.shouldBeInstanceOf<PreviewTreeNode.Group>()
            e.groupName shouldBe "e"
            // e.f
            val f = e.children.getOrNull(0)
            f.shouldBeInstanceOf<PreviewTreeNode.Preview>()
            f.preview shouldBe previews[4]
            // e
            val e2 = tree.getOrNull(2)
            e2.shouldBeInstanceOf<PreviewTreeNode.Preview>()
            e2.preview shouldBe previews[5]
            // e.a
            val eA = e.children.getOrNull(1)
            eA.shouldBeInstanceOf<PreviewTreeNode.Group>()
            eA.groupName shouldBe "a"
            // e.a.b
            val eAB = eA.children.getOrNull(0)
            eAB.shouldBeInstanceOf<PreviewTreeNode.Preview>()
            eAB.preview shouldBe previews[6]
        }

        "testCollapse" {
            val aBC = previewForTest("a.b.c")
            val tree = mutableListOf<PreviewTreeNode>(
                PreviewTreeNode.Group(
                    "a",
                    mutableListOf(
                        PreviewTreeNode.Group(
                            "b",
                            mutableListOf(PreviewTreeNode.Preview(aBC)),
                        ),
                    ),
                ),
            )
            val collapsedTree = tree.collapse()

            // a.b
            val aB = collapsedTree.getOrNull(0)
            aB.shouldBeInstanceOf<PreviewTreeNode.Group>()
            aB.groupName shouldBe "a.b"
            // a.b.c
            aB.children shouldBe mutableListOf<PreviewTreeNode>(
                PreviewTreeNode.Preview(aBC),
            )
        }

        "toTree should contain all previews" {
            forAll(Arb.list(Arb.string(1..20), 1..50)) { displayNames ->
                val previews = displayNames.map { previewForTest(it) }
                val tree = previews.toTree()
                val previewsInTree = tree.collectAllPreviews()
                previewsInTree.size == previews.size &&
                    previewsInTree.map { it.displayName }.toSet() == previews.map { it.displayName }.toSet()
            }
        }

        "collapse should preserve all previews" {
            forAll(Arb.list(Arb.string(1..20), 1..50)) { displayNames ->
                val previews = displayNames.map { previewForTest(it) }
                val tree = previews.toTree()
                val collapsedTree = tree.collapse()
                val previewsBeforeCollapse = tree.collectAllPreviews()
                val previewsAfterCollapse = collapsedTree.collectAllPreviews()
                previewsBeforeCollapse.size == previewsAfterCollapse.size &&
                    previewsBeforeCollapse.map { it.displayName }.toSet() ==
                    previewsAfterCollapse.map { it.displayName }.toSet()
            }
        }
    })

private fun previewForTest(displayName: String): PreviewLabPreview = CollectedPreview(
    displayName,
    displayName,
    "src/commonMain/kotlin/${displayName.replace(".", "/")}.kt",
) { }

private fun List<PreviewTreeNode>.collectAllPreviews(): List<PreviewLabPreview> = flatMap { node ->
    when (node) {
        is PreviewTreeNode.Preview -> listOf(node.preview)
        is PreviewTreeNode.Group -> node.children.collectAllPreviews()
    }
}
