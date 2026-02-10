package me.tbsten.compose.preview.lab.extension.debugger.debugtool

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.extension.debugger.DebugTool
import me.tbsten.compose.preview.lab.ui.Blue700
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButton
import me.tbsten.compose.preview.lab.ui.components.PreviewLabButtonVariant
import me.tbsten.compose.preview.lab.ui.components.PreviewLabChip
import me.tbsten.compose.preview.lab.ui.components.PreviewLabText

/**
 * A simple logger [DebugTool] that displays log messages in the debug menu.
 *
 * This is a built-in debug tool that works as a logger and displays messages
 * in the debug menu UI.
 *
 * Example usage:
 * ```kotlin
 * object AppDebugMenu : DebugMenu() {
 *     val logger = tool { SimpleLogger() }
 * }
 *
 * // Log messages from anywhere in your app
 * AppDebugMenu.logger.info("Updated")
 * AppDebugMenu.logger.warn("Invalid state", currentState)
 * AppDebugMenu.logger.error("Error", error = error)
 * ```
 *
 * @param printLog If true, logs will also be printed to the console via `println()`.
 * @param onLog Callback invoked when a log entry is added. Can be used for custom logging integrations.
 *
 * @see DebugTool
 * @see DebugMenu.tool
 */
class SimpleLogger(var printLog: Boolean = false, private val onLog: (LogEntry) -> Unit = {}) : DebugTool {
    override val title: String = "Logger"

    private var nextId = 0L
    private val _logs = mutableStateListOf<LogEntry>()
    private var enabledLevels by mutableStateOf(Level.entries.toSet())

    /**
     * Returns all log entries in reverse chronological order (newest first).
     */
    val logs: List<LogEntry> get() = _logs.reversed()

    /**
     * Returns filtered log entries based on enabled levels.
     */
    private val filteredLogs: List<LogEntry>
        get() = logs.filter { it.level in enabledLevels }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        SelectionContainer {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Header(
                    logCount = filteredLogs.size,
                    totalCount = logs.size,
                    onClear = { clear() },
                )
                LevelFilter(
                    enabledLevels = enabledLevels,
                    onToggleLevel = { level ->
                        enabledLevels = if (level in enabledLevels) {
                            enabledLevels - level
                        } else {
                            enabledLevels + level
                        }
                    },
                )
                LogList(
                    logs = filteredLogs,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    @Composable
    private fun Header(logCount: Int, totalCount: Int, onClear: () -> Unit, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PreviewLabText(
                text = if (logCount == totalCount) "Logs: $logCount" else "Logs: $logCount / $totalCount",
                style = PreviewLabTheme.typography.body1,
            )
            PreviewLabButton(
                text = "Clear",
                variant = PreviewLabButtonVariant.PrimaryGhost,
                onClick = onClear,
            )
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun LevelFilter(enabledLevels: Set<Level>, onToggleLevel: (Level) -> Unit, modifier: Modifier = Modifier) {
        FlowRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Level.entries.forEach { level ->
                PreviewLabChip(
                    selected = level in enabledLevels,
                    onClick = { onToggleLevel(level) },
                ) {
                    PreviewLabText(
                        text = level.name,
                        style = PreviewLabTheme.typography.label2,
                    )
                }
            }
        }
    }

    @Composable
    private fun LogList(logs: List<LogEntry>, modifier: Modifier = Modifier) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(logs, key = { it.id }) { entry ->
                LogEntryItem(entry, modifier = Modifier.animateItem())
            }
        }
    }

    @Composable
    private fun LogEntryItem(entry: LogEntry, modifier: Modifier = Modifier) {
        val (backgroundColor, textColor) = entry.level.colors()
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(4.dp))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            PreviewLabText(
                text = entry.format(),
                color = textColor,
                style = PreviewLabTheme.typography.body2,
            )
        }
    }

    @Composable
    private fun Level.colors(): Pair<Color, Color> = when (this) {
        Level.Debug -> PreviewLabTheme.colors.surface to PreviewLabTheme.colors.textSecondary
        Level.Info -> Blue700.copy(alpha = 0.1f) to Blue700
        Level.Warn -> Color(0xFFFFA500).copy(alpha = 0.1f) to Color(0xFFCC8400)
        Level.Error -> PreviewLabTheme.colors.error.copy(alpha = 0.1f) to PreviewLabTheme.colors.error
    }

    /**
     * Logs a message with the specified level.
     *
     * @param level The log level
     * @param messages The messages to log (will be concatenated)
     * @param error An optional throwable to include in the log
     */
    fun log(level: Level, vararg messages: Any?, error: Throwable? = null) {
        val entry = LogEntry(
            id = nextId++,
            level = level,
            messages = messages.map { it.toString() },
            error = error,
        )
        _logs.add(entry)
        if (printLog) {
            println(entry.format())
        }
        onLog(entry)
    }

    /**
     * Logs a debug-level message.
     */
    fun debug(vararg messages: Any?) = log(Level.Debug, *messages)

    /**
     * Logs an info-level message.
     */
    fun info(vararg messages: Any?) = log(Level.Info, *messages)

    /**
     * Logs a warning-level message.
     */
    fun warn(vararg messages: Any?) = log(Level.Warn, *messages)

    /**
     * Logs an error-level message with an optional throwable.
     */
    fun error(vararg messages: Any?, error: Throwable? = null) = log(Level.Error, *messages, error = error)

    /**
     * Clears all log entries.
     */
    fun clear() {
        _logs.clear()
    }

    /**
     * Represents a single log entry.
     */
    data class LogEntry(val id: Long, val level: Level, val messages: List<String>, val error: Throwable?) {
        /**
         * Formats this log entry as a string.
         */
        fun format(): String = buildString {
            append("[${level.name}] ")
            append(messages.joinToString(" "))
            if (error != null) {
                append("\n")
                append(error.stackTraceToString())
            }
        }
    }

    /**
     * Log levels supported by [SimpleLogger].
     */
    enum class Level {
        Debug,
        Info,
        Warn,
        Error,
    }
}
