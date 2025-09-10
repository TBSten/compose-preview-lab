package me.tbsten.compose.preview.lab.component

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import me.tbsten.compose.preview.lab.CollectedPreview
import me.tbsten.compose.preview.lab.previewlist.PreviewTreeNode
import me.tbsten.compose.preview.lab.previewlist.collapse
import me.tbsten.compose.preview.lab.previewlist.toTree

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
        assertEquals(aBC.collectedPreview, previews[0])
        // a.b.d
        val aBD = aB.children.getOrNull(1)
        assertIs<PreviewTreeNode.Group>(aBD)
        assertEquals(aBD.groupName, "d")
        // a.b.d.x
        val aBDX = aBD.children.getOrNull(0)
        assertIs<PreviewTreeNode.Preview>(aBDX)
        assertEquals(aBDX.collectedPreview, previews[1])
        // a.b.e
        val aBE = aB.children.getOrNull(2)
        assertIs<PreviewTreeNode.Preview>(aBE)
        assertEquals(aBE.collectedPreview, previews[2])
        // a.b
        val aB2 = a.children.getOrNull(1)
        assertIs<PreviewTreeNode.Preview>(aB2)
        assertEquals(aB2.collectedPreview, previews[3])
        // e
        val e = tree.getOrNull(1)
        assertIs<PreviewTreeNode.Group>(e)
        assertEquals(e.groupName, "e")
        // e.f
        val f = e.children.getOrNull(0)
        assertIs<PreviewTreeNode.Preview>(f)
        assertEquals(f.collectedPreview, previews[4])
        // e
        val e2 = tree.getOrNull(2)
        assertIs<PreviewTreeNode.Preview>(e2)
        assertEquals(e2.collectedPreview, previews[5])
        // e.a
        val eA = e.children.getOrNull(1)
        assertIs<PreviewTreeNode.Group>(eA)
        assertEquals(eA.groupName, "a")
        // e.a.b
        val eAB = eA.children.getOrNull(0)
        assertIs<PreviewTreeNode.Preview>(eAB)
        assertEquals(eAB.collectedPreview, previews[6])
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
}

private fun previewForTest(displayName: String): CollectedPreview = CollectedPreview(
    "{{qualifiedName}}",
    displayName,
    "src/commonMain/kotlin/${displayName.replace(".", "/")}.kt",
) { }
