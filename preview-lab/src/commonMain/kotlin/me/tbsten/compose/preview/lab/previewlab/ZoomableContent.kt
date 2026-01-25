package me.tbsten.compose.preview.lab.previewlab

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Preview コンテンツにズームジェスチャーを適用する Composable。
 *
 * - Zoomable 対応プラットフォーム (Android, iOS, JVM, WasmJS):
 *   ピンチジェスチャー、ダブルタップ、マウスホイールでのズームをサポート
 * - JS (Browser): 従来通りのドラッグ + graphicsLayer 実装を使用
 *
 * @param state PreviewLabState インスタンス
 * @param modifier Modifier
 * @param content コンテンツ
 */
@Composable
internal expect fun ZoomableContent(state: PreviewLabState, modifier: Modifier = Modifier, content: @Composable () -> Unit,)
