package me.tbsten.compose.preview.lab.sample

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.property.PropertyTesting

/**
 * Kotest project configuration for property-based tests with Compose UI.
 *
 * UI tests with Compose are expensive, so we reduce the default
 * iteration count from 1000 to a smaller value for better test performance.
 *
 * The iteration count can be configured via gradle.properties:
 * ```
 * test.property.iterations=20
 * ```
 */
object PropertyTestConfig : AbstractProjectConfig() {
    override suspend fun beforeProject() {
        PropertyTesting.defaultIterationCount = BuildKonfig.PROPERTY_TEST_ITERATIONS
    }
}
