package me.tbsten.compose.preview.lab.compiler.compat

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.forAll

class KotlinToolingVersionTest :
    StringSpec({
        "parses a stable version" {
            val v = KotlinToolingVersion("2.3.21")
            v.major shouldBe 2
            v.minor shouldBe 3
            v.patch shouldBe 21
            v.classifier shouldBe null
            v.maturity shouldBe KotlinToolingVersion.Maturity.STABLE
        }

        "parses a Beta classifier" {
            val v = KotlinToolingVersion("2.4.0-Beta2")
            v.classifier shouldBe "Beta2"
            v.maturity shouldBe KotlinToolingVersion.Maturity.BETA
            v.isDev shouldBe false
        }

        "parses a dev classifier" {
            val v = KotlinToolingVersion("2.4.0-dev-2124")
            v.maturity shouldBe KotlinToolingVersion.Maturity.DEV
            v.isDev shouldBe true
        }

        "parses an RC classifier" {
            KotlinToolingVersion("2.3.21-RC").maturity shouldBe KotlinToolingVersion.Maturity.RC
            KotlinToolingVersion("2.3.21-RC2").maturity shouldBe KotlinToolingVersion.Maturity.RC
        }

        "compare: patch order" {
            KotlinToolingVersion("2.3.0") shouldBeLessThan KotlinToolingVersion("2.3.10")
            KotlinToolingVersion("2.3.10") shouldBeLessThan KotlinToolingVersion("2.3.20")
            KotlinToolingVersion("2.3.20") shouldBeLessThan KotlinToolingVersion("2.3.21")
        }

        "compare: minor order" {
            KotlinToolingVersion("2.3.21") shouldBeLessThan KotlinToolingVersion("2.4.0-Beta2")
        }

        "compare: maturity (Beta < STABLE)" {
            KotlinToolingVersion("2.4.0-Beta2") shouldBeLessThan KotlinToolingVersion("2.4.0")
        }

        "compare: classifier number (Beta1 < Beta2)" {
            KotlinToolingVersion("2.4.0-Beta1") shouldBeLessThan KotlinToolingVersion("2.4.0-Beta2")
        }

        "compare: STABLE vs RC" {
            KotlinToolingVersion("2.3.0-RC") shouldBeLessThan KotlinToolingVersion("2.3.0")
        }

        "compare: same patch — 2.3.21 > 2.3.21-Beta2" {
            KotlinToolingVersion("2.3.21") shouldBeGreaterThan KotlinToolingVersion("2.3.21-Beta2")
        }

        "compare: RC without number vs RC with number (RC < RC2)" {
            KotlinToolingVersion("2.3.0-RC") shouldBeLessThan KotlinToolingVersion("2.3.0-RC2")
        }

        "toString roundtrip" {
            KotlinToolingVersion("2.3.21").toString() shouldBe "2.3.21"
            KotlinToolingVersion("2.4.0-Beta2").toString() shouldBe "2.4.0-Beta2"
        }

        // --- Property-Based Tests ---

        "PBT: compareTo is reflexive".config(tags = setOf(PBT)) {
            forAll(versionArb()) { v ->
                v.compareTo(v) == 0
            }
        }

        "PBT: compareTo is antisymmetric".config(tags = setOf(PBT)) {
            forAll(versionArb(), versionArb()) { a, b ->
                val ab = a.compareTo(b)
                val ba = b.compareTo(a)
                (ab == 0 && ba == 0) || (ab > 0 && ba < 0) || (ab < 0 && ba > 0)
            }
        }

        "PBT: equals implies same compareTo".config(tags = setOf(PBT)) {
            forAll(versionArb()) { v ->
                val copy = KotlinToolingVersion(v.major, v.minor, v.patch, v.classifier)
                v == copy && v.compareTo(copy) == 0
            }
        }

        "PBT: major bump is always newer".config(tags = setOf(PBT)) {
            forAll(Arb.int(0, 10), Arb.int(0, 20), Arb.int(0, 30)) { major, minor, patch ->
                val v = KotlinToolingVersion(major, minor, patch, null)
                val newer = KotlinToolingVersion(major + 1, minor, patch, null)
                v < newer
            }
        }

        "PBT: minor bump is always newer within same major".config(tags = setOf(PBT)) {
            forAll(Arb.int(0, 10), Arb.int(0, 19), Arb.int(0, 30)) { major, minor, patch ->
                val v = KotlinToolingVersion(major, minor, patch, null)
                val newer = KotlinToolingVersion(major, minor + 1, patch, null)
                v < newer
            }
        }

        "PBT: patch bump is always newer within same major.minor".config(tags = setOf(PBT)) {
            forAll(Arb.int(0, 10), Arb.int(0, 20), Arb.int(0, 29)) { major, minor, patch ->
                val v = KotlinToolingVersion(major, minor, patch, null)
                val newer = KotlinToolingVersion(major, minor, patch + 1, null)
                v < newer
            }
        }

        "PBT: stable version (null classifier) is newer than any pre-release of same triplet"
            .config(tags = setOf(PBT)) {
                forAll(preReleaseVersionArb()) { v ->
                    val stable = KotlinToolingVersion(v.major, v.minor, v.patch, null)
                    v < stable
                }
            }
    })

private val CLASSIFIERS = listOf("RC", "RC2", "Beta1", "Beta2", "Alpha1", "dev-1234", "snapshot")

/** Generates a [KotlinToolingVersion] with null classifier (stable). */
private fun stableVersionArb(): Arb<KotlinToolingVersion> = io.kotest.property.arbitrary.arbitrary {
    KotlinToolingVersion(Arb.int(0, 5).bind(), Arb.int(0, 20).bind(), Arb.int(0, 30).bind(), null)
}

/** Generates a [KotlinToolingVersion] with a non-null pre-release classifier. */
private fun preReleaseVersionArb(): Arb<KotlinToolingVersion> = io.kotest.property.arbitrary.arbitrary {
    val classifier = CLASSIFIERS[Arb.int(0, CLASSIFIERS.lastIndex).bind()]
    KotlinToolingVersion(Arb.int(0, 5).bind(), Arb.int(0, 20).bind(), Arb.int(0, 30).bind(), classifier)
}

/** Generates any [KotlinToolingVersion] (stable or pre-release). */
private fun versionArb(): Arb<KotlinToolingVersion> = io.kotest.property.arbitrary.arbitrary {
    if (Arb.int(0, 1).bind() == 0) stableVersionArb().bind() else preReleaseVersionArb().bind()
}
