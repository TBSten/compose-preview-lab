package me.tbsten.compose.preview.lab.previewlab.header

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

/**
 * `nextZoomInScale` / `nextZoomOutScale` の range fallback を検証する。
 *
 * 旧実装は `in Float.MIN_VALUE..<1.0f` で 0 / 負数 / `Float.MAX_VALUE` を取りこぼし
 * `TODO("Zoom value is out of range: ...")` でクラッシュしていた。
 */
class ZoomTest :
    StringSpec({
        val tolerance = 0.0001f

        "nextZoomInScale: 0f は TODO にならず 0.1x 加算される" {
            0f.nextZoomInScale() shouldBe (0.10f plusOrMinus tolerance)
        }

        "nextZoomOutScale: 0f は TODO にならず 0.1x 減算される" {
            0f.nextZoomOutScale() shouldBe (-0.10f plusOrMinus tolerance)
        }

        "nextZoomInScale: 負数も TODO にならず 0.1x 加算される" {
            (-1f).nextZoomInScale() shouldBe (-0.90f plusOrMinus tolerance)
        }

        "nextZoomOutScale: 負数も TODO にならず 0.1x 減算される" {
            (-1f).nextZoomOutScale() shouldBe (-1.10f plusOrMinus tolerance)
        }

        "nextZoomInScale: Float.MAX_VALUE は TODO にならず有限値を返す" {
            // Float.MAX_VALUE は range `2.0f..<POSITIVE_INFINITY` 内なので第 3 枝に入る。
            // 精度限界で 1.0f 加算は吸収され `Float.MAX_VALUE` のままだが TODO は投げない。
            val result = Float.MAX_VALUE.nextZoomInScale()
            result.isFinite() shouldBe true
        }

        "nextZoomOutScale: Float.MAX_VALUE は TODO にならず有限値を返す" {
            val result = Float.MAX_VALUE.nextZoomOutScale()
            result.isFinite() shouldBe true
        }

        "nextZoomInScale: 通常レンジ 1.0x は 1.25x" {
            1.0f.nextZoomInScale() shouldBe (1.25f plusOrMinus tolerance)
        }

        "nextZoomOutScale: 通常レンジ 2.0x は 1.0x" {
            2.0f.nextZoomOutScale() shouldBe (1.0f plusOrMinus tolerance)
        }

        "nextZoomInScale: NaN は TODO にならず fallback に落ちる" {
            // NaN は どの range 比較でも false なので else 枝 (coerceIn) に落ちる。
            // Float.coerceIn は NaN を返すため、 例外を投げないことのみ保証する。
            val result = Float.NaN.nextZoomInScale()
            result.isNaN() shouldBe true
        }

        "nextZoomInScale: POSITIVE_INFINITY も TODO にならず fallback で MaxZoomScale に丸まる" {
            // 全 range が exclusive(POSITIVE_INFINITY) なので else 枝に落ちる
            Float.POSITIVE_INFINITY.nextZoomInScale() shouldBe MaxZoomScale
        }
    })
