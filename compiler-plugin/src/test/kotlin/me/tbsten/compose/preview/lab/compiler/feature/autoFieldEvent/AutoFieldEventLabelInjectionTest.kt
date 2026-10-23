package me.tbsten.compose.preview.lab.compiler.feature.autoFieldEvent

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import me.tbsten.compose.preview.lab.compiler.CompilerPluginTestBase

/**
 * Tests for the IR pass that injects parameter names as `label` arguments of unlabelled
 * `autoField()` / `autoEvent()` call sites.
 *
 * Each test compiles a runtime stub of `PreviewLabScope.autoField` / `autoEvent` that
 * records its `label` parameter into a shared list. The test then invokes the generated
 * `callIt()` function and inspects the recorded list, which is the IR-transform output
 * encoded as runtime behavior. (Inspecting the compiled bytecode directly would couple
 * the test to JVM signature mangling and break across Kotlin versions.)
 */
class AutoFieldEventLabelInjectionTest :
    FunSpec({
        val base = CompilerPluginTestBase()

        // Runtime stubs used by every test. Mirrors the FQN that the IR pass matches
        // (`me.tbsten.compose.preview.lab.previewlab.autoField` / `autoEvent`).
        val autoStubs = SourceFile.kotlin(
            "AutoStubs.kt",
            """
            package me.tbsten.compose.preview.lab.previewlab

            object AutoLabelRecorder {
                @JvmStatic val labels: MutableList<String> = mutableListOf()
            }

            class PreviewLabScope

            fun PreviewLabScope.autoField(label: String = "auto"): String {
                AutoLabelRecorder.labels.add(label)
                return ""
            }

            fun PreviewLabScope.autoEvent(label: String = "auto"): () -> Unit = {
                AutoLabelRecorder.labels.add(label)
            }
            """,
        )

        fun loadCallIt(result: JvmCompilationResult): () -> Unit = {
            result.classLoader
                .loadClass("test.entry.EntryKt")
                .getMethod("callIt")
                .invoke(null)
        }

        fun loadRecordedLabels(result: JvmCompilationResult): List<String> {
            // `object AutoLabelRecorder { @JvmStatic val labels }` compiles to:
            //   public static MutableList<String> getLabels()  (on the class, because of @JvmStatic)
            // We invoke it via reflection so the test does not depend on whether the
            // backing field is exposed directly.
            @Suppress("UNCHECKED_CAST")
            return result.classLoader
                .loadClass("me.tbsten.compose.preview.lab.previewlab.AutoLabelRecorder")
                .getMethod("getLabels")
                .invoke(null) as List<String>
        }

        test("autoField() inside a named call gets the parameter name injected as label") {
            val result = base.compile(
                autoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoField

                    fun userCard(name: String, age: String) {}

                    fun callIt() {
                        with(PreviewLabScope()) {
                            userCard(
                                name = autoField(),
                                age = autoField(),
                            )
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            loadCallIt(result).invoke()
            loadRecordedLabels(result) shouldContainExactly listOf("name", "age")
        }

        test("autoEvent() inside a named call gets the parameter name injected as label") {
            val result = base.compile(
                autoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoEvent

                    fun buttonView(onClick: () -> Unit, onLongClick: () -> Unit) {
                        // Force the captured labels to be observed by invoking the lambdas.
                        onClick()
                        onLongClick()
                    }

                    fun callIt() {
                        with(PreviewLabScope()) {
                            buttonView(
                                onClick = autoEvent(),
                                onLongClick = autoEvent(),
                            )
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            loadCallIt(result).invoke()
            loadRecordedLabels(result) shouldContainExactly listOf("onClick", "onLongClick")
        }

        test("autoField(label = explicit) is left untouched by the compiler plugin") {
            val result = base.compile(
                autoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoField

                    fun userCard(name: String) {}

                    fun callIt() {
                        with(PreviewLabScope()) {
                            userCard(name = autoField(label = "explicit-label"))
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            loadCallIt(result).invoke()
            loadRecordedLabels(result) shouldContainExactly listOf("explicit-label")
        }

        test("autoField() at a positional argument slot still picks up the parameter name") {
            val result = base.compile(
                autoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoField

                    fun userCard(name: String, age: String) {}

                    fun callIt() {
                        with(PreviewLabScope()) {
                            userCard(autoField(), autoField())
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            loadCallIt(result).invoke()
            loadRecordedLabels(result) shouldContainExactly listOf("name", "age")
        }

        // Generic stubs for the type-driven rewrites. `autoField` itself throws so a test fails
        // loudly if the plugin leaves an enum call site untouched; the enum entry points record
        // the label and the reified entries they see.
        val typedAutoStubs = SourceFile.kotlin(
            "AutoStubs.kt",
            """
            package me.tbsten.compose.preview.lab.previewlab

            object AutoLabelRecorder {
                @JvmStatic val labels: MutableList<String> = mutableListOf()
            }

            class PreviewLabScope

            inline fun <reified T> PreviewLabScope.autoField(label: String = "auto"): T {
                AutoLabelRecorder.labels.add("autoField:" + label)
                throw IllegalStateException("autoField<" + T::class.simpleName + ">() was not rewritten")
            }

            inline fun <reified E : Enum<E>> PreviewLabScope.autoEnumField(label: String = "auto"): E {
                AutoLabelRecorder.labels.add("enum:" + label + ":" + enumValues<E>().joinToString(","))
                return enumValues<E>().first()
            }

            inline fun <reified E : Enum<E>> PreviewLabScope.autoNullableEnumField(label: String = "auto"): E? {
                AutoLabelRecorder.labels.add("nullableEnum:" + label + ":" + enumValues<E>().joinToString(","))
                return null
            }
            """,
        )

        test("autoField<E>() with an enum type argument is rewritten to autoEnumField<E>()") {
            val result = base.compile(
                typedAutoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoField

                    enum class Plan { Free, Pro }

                    fun planBadge(plan: Plan) {}

                    fun callIt() {
                        with(PreviewLabScope()) {
                            planBadge(plan = autoField())
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            loadCallIt(result).invoke()
            loadRecordedLabels(result) shouldContainExactly listOf("enum:plan:Free,Pro")
        }

        test("autoField<E?>() with a nullable enum type argument is rewritten to autoNullableEnumField<E>()") {
            val result = base.compile(
                typedAutoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoField

                    enum class Plan { Free, Pro }

                    fun planBadge(plan: Plan?) {}

                    fun callIt() {
                        with(PreviewLabScope()) {
                            planBadge(plan = autoField())
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            loadCallIt(result).invoke()
            loadRecordedLabels(result) shouldContainExactly listOf("nullableEnum:plan:Free,Pro")
        }

        test("standalone autoField<E>() keeps the default label but is still rewritten to autoEnumField") {
            val result = base.compile(
                typedAutoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoField

                    enum class Plan { Free, Pro }

                    fun callIt() {
                        with(PreviewLabScope()) {
                            val plan: Plan = autoField()
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            loadCallIt(result).invoke()
            loadRecordedLabels(result) shouldContainExactly listOf("enum:auto:Free,Pro")
        }

        test("autoField<T>() with a class the runtime has no default Field for reports a warning") {
            val result = base.compile(
                typedAutoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoField

                    class User(val name: String)

                    fun userCard(user: User) {}

                    fun callIt() {
                        with(PreviewLabScope()) {
                            userCard(user = autoField())
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            result.messages shouldContain "[ComposePreviewLab/IR,INVALID_USAGE]"
            result.messages shouldContain "autoField<test.entry.User>() has no default Field"
            // The call site location is attached so IDE / build output can jump to it.
            result.messages shouldContain "Entry.kt"
        }

        test("autoField<T>() with supported primitives (nullable or not) reports no warning") {
            val result = base.compile(
                typedAutoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoField

                    fun userCard(name: String, nickname: String?, age: Int, isPremium: Boolean?) {}

                    fun callIt() {
                        with(PreviewLabScope()) {
                            userCard(name = autoField(), nickname = autoField(), age = autoField(), isPremium = autoField())
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            result.messages shouldNotContain "has no default Field"
        }

        test("autoField<T>() inside a generic helper (type parameter) reports no warning") {
            val result = base.compile(
                typedAutoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoField

                    inline fun <reified T> PreviewLabScope.helper(): T = autoField()

                    fun callIt() {
                        with(PreviewLabScope()) {
                            val x: String = helper()
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            result.messages shouldNotContain "has no default Field"
        }

        test("standalone autoField() outside a call argument keeps the runtime default label") {
            val result = base.compile(
                autoStubs,
                SourceFile.kotlin(
                    "Entry.kt",
                    """
                    package test.entry

                    import me.tbsten.compose.preview.lab.previewlab.PreviewLabScope
                    import me.tbsten.compose.preview.lab.previewlab.autoField

                    fun callIt() {
                        with(PreviewLabScope()) {
                            val x: String = autoField()
                        }
                    }
                    """,
                ),
            )
            result.exitCode shouldBe KotlinCompilation.ExitCode.OK
            loadCallIt(result).invoke()
            loadRecordedLabels(result) shouldContainExactly listOf("auto")
        }
    })
