package me.tbsten.compose.preview.lab.previewlab.header

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

/**
 * `nextZoomInScale` / `nextZoomOutScale` の sanitize / clamp 仕様を検証する。
 *
 * 旧実装は `in Float.MIN_VALUE..<1.0f` で 0 / 負数 / `Float.MAX_VALUE` を取りこぼし
 * `TODO("Zoom value is out of range: ...")` でクラッシュしていた。
 * 現実装は不正入力 (NaN, +/-Infinity, 負数, 範囲外) を `[MinZoomScale, MaxZoomScale]` に
 * sanitize し、結果も同 range に clamp するため、 negative や non-finite が出力に漏れない。
 *
 * これは `ContentSection` の grid 描画 (`while (gridX <= size.width) { gridX += gridSize }`)
 * を非正な `gridSize` で無限ループさせないために重要。
 */
class ZoomTest :
    StringSpec({
        val tolerance = 0.0001f

        // --- 通常レンジ ---

        "nextZoomInScale: 通常レンジ 1.0x は 1.25x" {
            1.0f.nextZoomInScale() shouldBe (1.25f plusOrMinus tolerance)
        }

        "nextZoomOutScale: 通常レンジ 2.0x は 1.0x" {
            2.0f.nextZoomOutScale() shouldBe (1.0f plusOrMinus tolerance)
        }

        // --- 範囲外入力 (safe fallback: 結果は finite かつ [MinZoomScale, MaxZoomScale]) ---

        "nextZoomInScale: 0f は sanitize 後 MinZoomScale + 0.1 = 0.20f" {
            val result = 0f.nextZoomInScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
            result shouldBe ((MinZoomScale + 0.10f) plusOrMinus tolerance)
        }

        "nextZoomOutScale: 0f は sanitize 後 MinZoomScale にクランプ" {
            val result = 0f.nextZoomOutScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
            result shouldBe MinZoomScale
        }

        "nextZoomInScale: 負数 (-1f) は sanitize されて結果が範囲内" {
            val result = (-1f).nextZoomInScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
        }

        "nextZoomOutScale: 負数 (-1f) は sanitize されて結果が範囲内" {
            val result = (-1f).nextZoomOutScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
            result shouldBe MinZoomScale
        }

        "nextZoomInScale: Float.MAX_VALUE は sanitize されて結果が範囲内" {
            val result = Float.MAX_VALUE.nextZoomInScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
            result shouldBe MaxZoomScale
        }

        "nextZoomOutScale: Float.MAX_VALUE は sanitize されて結果が範囲内" {
            val result = Float.MAX_VALUE.nextZoomOutScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
        }

        // --- 非有限入力 (NaN / +Infinity / -Infinity も safe fallback) ---

        "nextZoomInScale: NaN は sanitize されて結果が範囲内" {
            val result = Float.NaN.nextZoomInScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
        }

        "nextZoomOutScale: NaN は sanitize されて結果が範囲内" {
            val result = Float.NaN.nextZoomOutScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
        }

        "nextZoomInScale: POSITIVE_INFINITY は sanitize されて結果が範囲内" {
            val result = Float.POSITIVE_INFINITY.nextZoomInScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
        }

        "nextZoomOutScale: POSITIVE_INFINITY は sanitize されて結果が範囲内" {
            val result = Float.POSITIVE_INFINITY.nextZoomOutScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
        }

        "nextZoomInScale: NEGATIVE_INFINITY は sanitize されて結果が範囲内" {
            val result = Float.NEGATIVE_INFINITY.nextZoomInScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
        }

        "nextZoomOutScale: NEGATIVE_INFINITY は sanitize されて結果が範囲内" {
            val result = Float.NEGATIVE_INFINITY.nextZoomOutScale()
            result.isFinite() shouldBe true
            (result in MinZoomScale..MaxZoomScale) shouldBe true
        }
    })
