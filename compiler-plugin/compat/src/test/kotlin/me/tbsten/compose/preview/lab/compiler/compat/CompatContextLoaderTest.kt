package me.tbsten.compose.preview.lab.compiler.compat

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Loader unit test. The full resolve logic is exercised end-to-end once the compat
 * modules are in place, so here we only verify behavior against the actual classpath
 * via `CompatContext.load(knownVersion)`.
 *
 * In this module's classpath there are no Factory implementations, so the test
 * confirms that a "No factories" error is raised.
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
