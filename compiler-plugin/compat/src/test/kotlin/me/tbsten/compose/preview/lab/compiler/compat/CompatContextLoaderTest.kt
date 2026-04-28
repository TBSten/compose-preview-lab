package me.tbsten.compose.preview.lab.compiler.compat

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Tests the error path when no [CompatContext.Factory] implementations are on the classpath.
 * (The compat module itself registers no factory; factories live in compiler-plugin/compat-k* modules.)
 */
class CompatContextLoaderTest :
    StringSpec({
        "throws when no factory is available" {
            val ex = shouldThrow<IllegalStateException> {
                CompatContext.load(KotlinToolingVersion("2.3.21"))
            }
            ex.message?.contains("No CompatContext.Factory") shouldBe true
        }
    })
