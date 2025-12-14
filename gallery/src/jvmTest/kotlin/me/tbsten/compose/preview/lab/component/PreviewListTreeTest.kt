package me.tbsten.compose.preview.lab.component

import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import me.tbsten.compose.preview.lab.CollectedPreview
import me.tbsten.compose.preview.lab.PreviewLabPreview
import me.tbsten.compose.preview.lab.gallery.previewlist.PreviewTreeNode
import me.tbsten.compose.preview.lab.gallery.previewlist.collapse
import me.tbsten.compose.preview.lab.gallery.previewlist.toTree

class PreviewListTreeTest {
    @Test
    fun testToTree() {
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
        assertIs<PreviewTreeNode.Group>(a)
        assertEquals(a.groupName, "a")
        // a.b
        val aB = a.children.getOrNull(0)
        assertIs<PreviewTreeNode.Group>(aB)
        assertEquals(aB.groupName, "b")
        // a.b.c
        val aBC = aB.children.getOrNull(0)
        assertIs<PreviewTreeNode.Preview>(aBC)
        assertEquals(aBC.preview, previews[0])
        // a.b.d
        val aBD = aB.children.getOrNull(1)
        assertIs<PreviewTreeNode.Group>(aBD)
        assertEquals(aBD.groupName, "d")
        // a.b.d.x
        val aBDX = aBD.children.getOrNull(0)
        assertIs<PreviewTreeNode.Preview>(aBDX)
        assertEquals(aBDX.preview, previews[1])
        // a.b.e
        val aBE = aB.children.getOrNull(2)
        assertIs<PreviewTreeNode.Preview>(aBE)
        assertEquals(aBE.preview, previews[2])
        // a.b
        val aB2 = a.children.getOrNull(1)
        assertIs<PreviewTreeNode.Preview>(aB2)
        assertEquals(aB2.preview, previews[3])
        // e
        val e = tree.getOrNull(1)
        assertIs<PreviewTreeNode.Group>(e)
        assertEquals(e.groupName, "e")
        // e.f
        val f = e.children.getOrNull(0)
        assertIs<PreviewTreeNode.Preview>(f)
        assertEquals(f.preview, previews[4])
        // e
        val e2 = tree.getOrNull(2)
        assertIs<PreviewTreeNode.Preview>(e2)
        assertEquals(e2.preview, previews[5])
        // e.a
        val eA = e.children.getOrNull(1)
        assertIs<PreviewTreeNode.Group>(eA)
        assertEquals(eA.groupName, "a")
        // e.a.b
        val eAB = eA.children.getOrNull(0)
        assertIs<PreviewTreeNode.Preview>(eAB)
        assertEquals(eAB.preview, previews[6])
    }

    @Test
    fun testCollapse() {
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
        assertIs<PreviewTreeNode.Group>(aB)
        assertEquals(aB.groupName, "a.b")
        // a.b.c
        assertEquals(
            aB.children,
            mutableListOf<PreviewTreeNode>(
                PreviewTreeNode.Preview(aBC),
            ),
        )
    }

    @Test
    fun `toTree should contain all previews`() = runTest {
        forAll(Arb.list(Arb.string(1..20), 1..50)) { displayNames ->
            val previews = displayNames.map { previewForTest(it) }
            val tree = previews.toTree()
            val previewsInTree = tree.collectAllPreviews()
            previewsInTree.size == previews.size &&
                previewsInTree.map { it.displayName }.toSet() == previews.map { it.displayName }.toSet()
        }
    }

    @Test
    fun `collapse should preserve all previews`() = runTest {
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
}

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
