package me.tbsten.compose.preview.lab.extension.debugger

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class DebugMenuTest :
    StringSpec({
        "tool should register tool immediately" {
            val menu = TestDebugMenu()

            // Tool should be registered immediately on instantiation
            menu.tools shouldHaveSize 1
            menu.tools[0].name shouldBe "Test Tool"
            menu.tools[0].tool shouldBe menu.testTool
        }

        "tool should return same instance on subsequent accesses" {
            val menu = TestDebugMenu()

            val tool1 = menu.testTool
            val tool2 = menu.testTool

            tool1 shouldBe tool2
        }

        "tool should register multiple tools" {
            val menu = MultiToolDebugMenu()

            menu.tools shouldHaveSize 3
            menu.tools.map { it.name } shouldContain "Tool 1"
            menu.tools.map { it.name } shouldContain "Tool 2"
            menu.tools.map { it.name } shouldContain "Tool 3"
        }

        "custom DebugTool should preserve concrete type" {
            val menu = MixedDebugMenu()

            // Access custom tool - type should be preserved
            val customTool: CustomDebugTool = menu.customTool

            // Can access custom properties
            customTool.customProperty shouldBe 42
        }

        "should register multiple custom DebugTools" {
            val menu = MixedDebugMenu()

            menu.tools shouldHaveSize 2
        }
    })

private class TestDebugMenu : DebugMenu() {
    val testTool = tool { SimpleDebugTool("Test Tool") }
}

private class MultiToolDebugMenu : DebugMenu() {
    val tool1 = tool { SimpleDebugTool("Tool 1") }
    val tool2 = tool { SimpleDebugTool("Tool 2") }
    val tool3 = tool { SimpleDebugTool("Tool 3") }
}

// Simple DebugTool implementation for testing
private class SimpleDebugTool(override val title: String) : DebugTool {
    @androidx.compose.runtime.Composable
    override fun Content() {
        // Empty for test
    }
}

// Custom DebugTool implementation for testing
private class CustomDebugTool : DebugTool {
    override val title: String = "Custom Tool"

    @androidx.compose.runtime.Composable
    override fun Content() {
        // Custom UI - empty for test
    }

    val customProperty: Int = 42
}

private class MixedDebugMenu : DebugMenu() {
    val anotherTool = tool { SimpleDebugTool("Another Tool") }
    val customTool = tool { CustomDebugTool() }
}
