package me.tbsten.compose.preview.lab.compiler.error

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import me.tbsten.compose.preview.lab.compiler.error.ComposePreviewLabCompilerPluginError.Category
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

/**
 * Tests for `report`, `throwAsException`, and the new `orThrow` helper. Covers:
 * - single-error rendering (categories prefix, message, description, context, replies)
 * - vararg `report` aggregation (multiple errors get a `複数のエラーが発生しました:` wrapper)
 * - vararg `throwAsException` wraps each sub-error via `addSuppressed`
 * - `orThrow` short-circuits to the receiver when non-null, throws otherwise
 * - boundary cases (size 0 / 1) for the vararg overloads
 *
 * No `kctfork` / `KotlinCompilation` setup needed — these are pure unit tests on the
 * structured-error layer.
 */
private data class Captured(
    val severity: CompilerMessageSeverity,
    val message: String,
    val location: CompilerMessageSourceLocation?
)

private class CapturingCollector : MessageCollector {
    val captured = mutableListOf<Captured>()
    override fun clear() {
        captured.clear()
    }
    override fun hasErrors(): Boolean = captured.any { it.severity == CompilerMessageSeverity.ERROR }
    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
        captured += Captured(severity, message, location)
    }
}

private class TestError(
    override val message: String,
    override val categories: List<Category> = listOf(Category.IR),
    override val description: String? = null,
    override val context: List<String> = emptyList(),
    override val replies: List<String> = listOf("test reply"),
) : ComposePreviewLabCompilerPluginError

class ReportErrorTest :
    FunSpec({

        // -----------------------------------------------------------------------------
        // Single-error rendering
        // -----------------------------------------------------------------------------

        test("report(error, location) emits CompilerMessageSeverity.ERROR with rendered body") {
            val collector = CapturingCollector()
            val error = TestError(
                message = "boom",
                categories = listOf(Category.IR, Category.PREVIEW_COLLECTION),
                description = "longer explanation",
                context = listOf("hash: abc", "isFlag"),
                replies = listOf("reply one", "reply two"),
            )

            collector.report(error, location = null)

            collector.captured shouldHaveSize 1
            val captured = collector.captured.single()
            captured.severity shouldBe CompilerMessageSeverity.ERROR
            captured.message shouldStartWith "[ComposePreviewLab/IR,PREVIEW_COLLECTION] boom"
            captured.message shouldContain "longer explanation"
            captured.message shouldContain "hash: abc"
            captured.message shouldContain "isFlag"
            captured.message shouldContain "reply one"
            captured.message shouldContain "reply two"
        }

        test("buildErrorBody omits empty sections cleanly") {
            val error = TestError(message = "minimal", description = null, context = emptyList(), replies = emptyList())
            val body = buildErrorBody(error)

            body shouldBe "[ComposePreviewLab/IR] minimal"
            body shouldNotContain "Context:"
            body shouldNotContain "How to reply:"
        }

        // -----------------------------------------------------------------------------
        // throwAsException (single)
        // -----------------------------------------------------------------------------

        test("throwAsException wraps the error inside ComposePreviewLabCompilerPluginException") {
            val error = TestError(message = "thrown")
            val ex = shouldThrow<ComposePreviewLabCompilerPluginException> { error.throwAsException() }
            ex.error shouldBe error
            ex.message!! shouldContain "[ComposePreviewLab/IR] thrown"
        }

        test("throwAsException supports a cause") {
            val error = TestError(message = "with cause")
            val cause = RuntimeException("inner")
            val ex = shouldThrow<ComposePreviewLabCompilerPluginException> { error.throwAsException(cause = cause) }
            ex.cause shouldBe cause
        }

        // -----------------------------------------------------------------------------
        // orThrow
        // -----------------------------------------------------------------------------

        test("orThrow returns the receiver when non-null without invoking the builder") {
            var builderCalled = false
            val value: String = "hoge".orThrow {
                builderCalled = true
                TestError(message = "should not throw")
            }

            value shouldBe "hoge"
            builderCalled shouldBe false
        }

        test("orThrow throws via throwAsException when the receiver is null") {
            val nullable: String? = null
            val ex = shouldThrow<ComposePreviewLabCompilerPluginException> {
                nullable.orThrow { TestError(message = "thrown via orThrow") }
            }
            ex.message!! shouldContain "thrown via orThrow"
        }

        // -----------------------------------------------------------------------------
        // vararg report
        // -----------------------------------------------------------------------------

        test("report(vararg errors) with 0 errors is a no-op") {
            val collector = CapturingCollector()
            collector.report(location = null)
            collector.captured shouldHaveSize 0
        }

        test("report(vararg errors) with 1 error delegates to the single overload (no aggregate wrapper)") {
            val collector = CapturingCollector()
            val error = TestError(message = "single", categories = listOf(Category.FIR))
            collector.report(error, location = null)
            val single = collector.captured.single().message

            collector.clear()
            collector.report(error, location = null)
            val varargSingle = collector.captured.single().message

            varargSingle shouldBe single
            varargSingle shouldNotContain "複数のエラーが発生しました"
        }

        test("report(vararg errors) with 2+ errors aggregates with the 複数のエラーが発生しました header") {
            val collector = CapturingCollector()
            val errorA = TestError(message = "A", categories = listOf(Category.IR))
            val errorB = TestError(message = "B", categories = listOf(Category.FIR))
            val errorC = TestError(message = "C", categories = listOf(Category.IR, Category.INVALID_USAGE))

            collector.report(errorA, errorB, errorC, location = null)
            val msg = collector.captured.single().message

            msg shouldStartWith "[ComposePreviewLab/IR,FIR,INVALID_USAGE] 複数のエラーが発生しました:"
            msg shouldContain "- A"
            msg shouldContain "- B"
            msg shouldContain "- C"
            msg shouldContain "Errors:"
        }

        // -----------------------------------------------------------------------------
        // vararg throwAsException
        // -----------------------------------------------------------------------------

        test("throwAsException(vararg) with 0 errors throws IllegalArgumentException for the caller bug") {
            shouldThrow<IllegalArgumentException> { throwAsException() }
        }

        test("throwAsException(vararg) with 1 error delegates to the single overload") {
            val error = TestError(message = "lone")
            val ex = shouldThrow<ComposePreviewLabCompilerPluginException> { throwAsException(error) }
            ex.error shouldBe error
            ex.suppressedExceptions shouldHaveSize 0
        }

        test("throwAsException(vararg) with 2+ errors aggregates via addSuppressed") {
            val errorA = TestError(message = "A")
            val errorB = TestError(message = "B")

            val ex = shouldThrow<ComposePreviewLabCompilerPluginException> { throwAsException(errorA, errorB) }
            val aggregate = ex.error.shouldBeInstanceOf<AggregateError>()
            aggregate.errors shouldBe listOf(errorA, errorB)
            ex.suppressedExceptions shouldHaveSize 2
            ex.suppressedExceptions[0].message!! shouldContain "A"
            ex.suppressedExceptions[1].message!! shouldContain "B"
        }
    })
