package me.tbsten.compose.preview.lab.sample.allfields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.field.LongField
import me.tbsten.compose.preview.lab.sample.OnValueChange
import me.tbsten.compose.preview.lab.sample.SpeechBubbleBox
import me.tbsten.compose.preview.lab.sample.speechBubble
import androidx.compose.ui.tooling.preview.Preview

internal enum class LongFieldExampleSteps {
    EditValue,
    SeeResult,
}

/**
 * Demonstrates [LongField] for editing large integer values.
 *
 * Enter file size in bytes; see human-readable conversion (KB, MB, GB).
 * Includes quota visualization and upload time estimation.
 */
@Preview
@ComposePreviewLabOption(id = "LongFieldExample")
@Composable
internal fun LongFieldExample() = PreviewLab {
    var step by remember { mutableStateOf(LongFieldExampleSteps.EditValue) }

    val timestamp = fieldState {
        LongField("File size (bytes)", 3_145_728L) // ~3MB
            .speechBubble(
                bubbleText = "1. Change bytes (Long)",
                alignment = Alignment.BottomStart,
                visible = { step == LongFieldExampleSteps.EditValue },
            )
    }.also { state ->
        OnValueChange(state) {
            step = LongFieldExampleSteps.SeeResult
        }
    }

    SpeechBubbleBox(
        bubbleText = "2. Human readable size updated!",
        visible = step == LongFieldExampleSteps.SeeResult,
        alignment = Alignment.BottomCenter,
    ) {
        FileSizePreview(bytes = timestamp.value)
    }
}

@Composable
internal fun FileSizePreview(bytes: Long) {
    val safe = bytes.coerceAtLeast(0L)
    val pretty = formatBytes(safe)
    val quota = 10L * 1024L * 1024L // 10MB
    val ratio = (safe.toDouble() / quota.toDouble()).coerceIn(0.0, 1.0)
    val bar = buildString {
        val filled = (ratio * 20).toInt()
        append("[")
        append("#".repeat(filled))
        append("-".repeat(20 - filled))
        append("]")
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(4.dp),
    ) {
        Text("Upload Preview", style = MaterialTheme.typography.titleMedium)
        Text("bytes: $bytes", style = MaterialTheme.typography.bodyMedium)
        Text("size: $pretty", style = MaterialTheme.typography.bodyMedium)
        Text("quota: ${formatBytes(quota)}", style = MaterialTheme.typography.bodySmall)
        Text("usage: $bar ${(ratio * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)

        if (safe > quota) {
            Text(
                "Over quota! Please compress the file.",
                color = Color(0xFFD32F2F),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("[OK]", color = Color(0xFF2E7D32))
                Text("Estimated upload: ~${estimateUploadSeconds(safe)}s", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun estimateUploadSeconds(bytes: Long): Int {
    // Pretend 1MB/s
    val bps = 1_048_576.0
    return (bytes / bps).toInt().coerceAtLeast(0)
}

private fun formatBytes(bytes: Long): String {
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var idx = 0
    while (value >= 1024.0 && idx < units.lastIndex) {
        value /= 1024.0
        idx++
    }
    return if (idx == 0) "${bytes}B" else "${fmt2(value)}${units[idx]}"
}

private fun fmt2(v: Double): String {
    val sign = if (v < 0) "-" else ""
    val abs = kotlin.math.abs(v)
    val scaled = (abs * 100.0 + 0.5).toLong()
    val intPart = scaled / 100
    val frac = (scaled % 100).toInt()
    return sign + intPart.toString() + "." + frac.toString().padStart(2, '0')
}
