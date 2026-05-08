package me.tbsten.compose.preview.lab.compiler.feature

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import me.tbsten.compose.preview.lab.compiler.ir.buildHashMapWithCollisionDetection

/**
 * Verifies the collision-detection seam used by `buildPreviewByHashMap`. Operates on
 * the generic [buildHashMapWithCollisionDetection] helper with a controlled hash
 * function so we can simulate true collisions without needing a real SHA-256 sweep.
 *
 * The integration of this helper with `IrSimpleFunction` lookup is exercised
 * end-to-end in `PreviewHintEmissionTest` and `CrossModuleAggregationTest`; this file
 * only covers the pure collision-detection logic.
 */
class HintHashCollisionTest :
    FunSpec({
        test("distinct canonical keys mapped to the same hash fire onCollision and last writer wins") {
            val hits = mutableListOf<Triple<String, String, String>>()
            val map = buildHashMapWithCollisionDetection(
                entries = listOf("keyA" to "A", "keyB" to "B"),
                hash = { "h" },
                onCollision = { hash, existing, conflicting ->
                    hits.add(Triple(hash, existing, conflicting))
                },
            )

            // Last entry wins on the silent overwrite.
            map shouldContainExactly mapOf("h" to "B")
            // Collision callback fires exactly once, naming both inputs.
            hits shouldHaveSize 1
            hits[0] shouldBe Triple("h", "A", "B")
        }

        test("repeating the same canonical key on the same hash is a silent idempotent overwrite, not a collision") {
            val hits = mutableListOf<String>()
            val map = buildHashMapWithCollisionDetection(
                entries = listOf("keyA" to "first", "keyA" to "second"),
                hash = { "h" },
                onCollision = { _, existing, conflicting -> hits.add("$existing/$conflicting") },
            )

            // Idempotent overwrite: last writer wins, no collision callback.
            map shouldContainExactly mapOf("h" to "second")
            hits shouldHaveSize 0
        }

        test("distinct keys mapped to distinct hashes coexist without firing onCollision") {
            val hits = mutableListOf<String>()
            val map = buildHashMapWithCollisionDetection(
                entries = listOf("keyA" to "A", "keyB" to "B"),
                hash = { it.uppercase() }, // KEYA, KEYB
                onCollision = { _, existing, conflicting -> hits.add("$existing/$conflicting") },
            )

            map shouldContainExactly mapOf("KEYA" to "A", "KEYB" to "B")
            hits shouldHaveSize 0
        }

        test("three-way collision on the same hash fires onCollision twice (each new arrival vs the previous winner)") {
            val hits = mutableListOf<Triple<String, String, String>>()
            val map = buildHashMapWithCollisionDetection(
                entries = listOf("k1" to "A", "k2" to "B", "k3" to "C"),
                hash = { "h" },
                onCollision = { hash, existing, conflicting ->
                    hits.add(Triple(hash, existing, conflicting))
                },
            )

            // Final writer wins.
            map shouldContainExactly mapOf("h" to "C")
            // 2 callbacks: "B" arrives → existing was "A"; "C" arrives → existing was "B".
            hits shouldHaveSize 2
            hits[0] shouldBe Triple("h", "A", "B")
            hits[1] shouldBe Triple("h", "B", "C")
        }

        test("empty entries list returns an empty map without firing onCollision") {
            var collisionFired = false
            val map = buildHashMapWithCollisionDetection<String>(
                entries = emptyList(),
                hash = { error("should not be called for empty input") },
                onCollision = { _, _, _ -> collisionFired = true },
            )

            map.isEmpty() shouldBe true
            collisionFired shouldBe false
        }
    })
