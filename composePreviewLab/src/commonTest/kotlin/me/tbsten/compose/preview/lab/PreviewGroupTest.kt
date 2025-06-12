package me.tbsten.compose.preview.lab

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PreviewGroupTest {
    
    @Test
    fun testSingleLevelPreviews() {
        val previews = listOf(
            CollectedPreview("Button", "Button.kt") { },
            CollectedPreview("Text", "Text.kt") { }
        )
        
        val result = previews.groupByDisplayName()
        
        assertEquals(2, result.size)
        assertTrue(result[0] is PreviewGroupItem.Preview)
        assertTrue(result[1] is PreviewGroupItem.Preview)
        assertEquals("Button", (result[0] as PreviewGroupItem.Preview).preview.displayName)
        assertEquals("Text", (result[1] as PreviewGroupItem.Preview).preview.displayName)
    }
    
    @Test
    fun testBasicGrouping() {
        val previews = listOf(
            CollectedPreview("UI.Button", "Button.kt") { },
            CollectedPreview("UI.Text", "Text.kt") { }
        )
        
        val result = previews.groupByDisplayName()
        
        assertEquals(1, result.size)
        assertTrue(result[0] is PreviewGroupItem.Group)
        val group = (result[0] as PreviewGroupItem.Group).group
        assertEquals("UI", group.name)
        assertEquals(2, group.children.size)
    }
    
    @Test
    fun testIntelliJStyleCollapsing() {
        val previews = listOf(
            CollectedPreview("com.example.ui.Button", "Button.kt") { }
        )
        
        val result = previews.groupByDisplayName()
        
        assertEquals(1, result.size)
        assertTrue(result[0] is PreviewGroupItem.Group)
        val group = (result[0] as PreviewGroupItem.Group).group
        assertEquals("com", group.name)
        assertEquals(1, group.children.size)
        
        assertTrue(group.children[0] is PreviewGroupItem.Group)
        val childGroup = (group.children[0] as PreviewGroupItem.Group).group
        assertEquals("example.ui.Button", childGroup.name)
        assertEquals(1, childGroup.children.size)
        assertTrue(childGroup.children[0] is PreviewGroupItem.Preview)
        assertEquals("com.example.ui.Button", (childGroup.children[0] as PreviewGroupItem.Preview).preview.displayName)
    }

    
    @Test
    fun testMixedGroupedAndUngrouped() {
        val previews = listOf(
            CollectedPreview("Button", "Button.kt") { },
            CollectedPreview("UI.Text", "Text.kt") { },
            CollectedPreview("UI.Icon", "Icon.kt") { }
        )
        
        val result = previews.groupByDisplayName()
        
        assertEquals(2, result.size)
        assertTrue(result[0] is PreviewGroupItem.Preview)
        assertEquals("Button", (result[0] as PreviewGroupItem.Preview).preview.displayName)
        assertTrue(result[1] is PreviewGroupItem.Group)
        val group = (result[1] as PreviewGroupItem.Group).group
        assertEquals("UI", group.name)
        assertEquals(2, group.children.size)
    }
    
    @Test
    fun testDeepNesting() {
        val previews = listOf(
            CollectedPreview("com.example.ui.components.buttons.PrimaryButton", "PrimaryButton.kt") { },
            CollectedPreview("com.example.ui.components.buttons.SecondaryButton", "SecondaryButton.kt") { }
        )
        
        val result = previews.groupByDisplayName()
        
        assertEquals(1, result.size)
        assertTrue(result[0] is PreviewGroupItem.Group)
        val topGroup = (result[0] as PreviewGroupItem.Group).group
        assertEquals("com", topGroup.name)
        assertEquals(1, topGroup.children.size)
        
        assertTrue(topGroup.children[0] is PreviewGroupItem.Group)
        val childGroup = (topGroup.children[0] as PreviewGroupItem.Group).group
        assertEquals("example.ui.components.buttons", childGroup.name)
        assertEquals(2, childGroup.children.size)
        
        assertTrue(childGroup.children[0] is PreviewGroupItem.Group)
        assertTrue(childGroup.children[1] is PreviewGroupItem.Group)
        val primaryGroup = (childGroup.children[0] as PreviewGroupItem.Group).group
        val secondaryGroup = (childGroup.children[1] as PreviewGroupItem.Group).group
        assertEquals("PrimaryButton", primaryGroup.name)
        assertEquals("SecondaryButton", secondaryGroup.name)
    }
    
    @Test
    fun testEmptyList() {
        val previews = emptyList<CollectedPreview>()
        val result = previews.groupByDisplayName()
        assertEquals(0, result.size)
    }
    
    @Test
    fun testSingleItem() {
        val previews = listOf(
            CollectedPreview("SinglePreview", "Single.kt") { }
        )
        
        val result = previews.groupByDisplayName()
        
        assertEquals(1, result.size)
        assertTrue(result[0] is PreviewGroupItem.Preview)
        assertEquals("SinglePreview", (result[0] as PreviewGroupItem.Preview).preview.displayName)
    }
    
    @Test
    fun testComplexHierarchy() {
        val previews = listOf(
            CollectedPreview("TopLevel", "TopLevel.kt") { },
            CollectedPreview("com.example.ui.Button", "Button.kt") { },
            CollectedPreview("com.example.ui.Text", "Text.kt") { },
            CollectedPreview("com.example.screens.Login", "Login.kt") { },
            CollectedPreview("com.example.screens.Profile", "Profile.kt") { }
        )
        
        val result = previews.groupByDisplayName()
        
        assertEquals(2, result.size)
        assertTrue(result[0] is PreviewGroupItem.Preview)
        assertEquals("TopLevel", (result[0] as PreviewGroupItem.Preview).preview.displayName)
        
        assertTrue(result[1] is PreviewGroupItem.Group)
        val comGroup = (result[1] as PreviewGroupItem.Group).group
        // Based on actual behavior: the top-level group is just "com"
        assertEquals("com", comGroup.name)
        assertEquals(1, comGroup.children.size)
        
        // The child should be a group for "example" with 2 child groups
        assertTrue(comGroup.children[0] is PreviewGroupItem.Group)
        val exampleGroup = (comGroup.children[0] as PreviewGroupItem.Group).group
        assertEquals("example", exampleGroup.name)
        assertEquals(2, exampleGroup.children.size)
        
        // Verify the child groups under example
        val childGroups = exampleGroup.children.filterIsInstance<PreviewGroupItem.Group>()
        assertEquals(2, childGroups.size)
        
        val groupNames = childGroups.map { it.group.name }.sorted()
        assertEquals(listOf("screens", "ui"), groupNames)
    }
}
