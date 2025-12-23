package me.tbsten.compose.preview.lab.sample

import io.kotest.property.PropertyTesting

/**
 * Base class for property-based tests with Compose UI.
 *
 * UI tests with Compose are expensive, so we reduce the default
 * iteration count from 1000 to a smaller value for better test performance.
 *
 * All test classes should extend this base class to ensure the default
 * iteration count is properly configured.
 *
 * The iteration count can be configured via gradle.properties:
 * ```
 * test.property.iterations=20
 * ```
 */
abstract class PropertyTestBase {
    init {
        PropertyTesting.defaultIterationCount = DEFAULT_ITERATIONS
    }

    companion object {
        /**
         * Default iteration count for property-based tests.
         * Configured via `test.property.iterations` in gradle.properties.
         */
        val DEFAULT_ITERATIONS: Int = BuildKonfig.PROPERTY_TEST_ITERATIONS
    }
}
