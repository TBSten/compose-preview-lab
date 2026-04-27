package me.tbsten.compose.preview.lab.compiler.compat

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe

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

        "toString roundtrip" {
            KotlinToolingVersion("2.3.21").toString() shouldBe "2.3.21"
            KotlinToolingVersion("2.4.0-Beta2").toString() shouldBe "2.4.0-Beta2"
        }
    })
