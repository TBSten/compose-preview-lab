package me.tbsten.compose.preview.lab.sample.debugmenu

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import me.tbsten.compose.preview.lab.extension.debugger.DebugTool

/**
 * Simple in-memory logger for debug menu.
 *
 * Usage:
 * ```kotlin
 * AppDebugMenu.logger.log("Something happened")
 * ```
 */
class SimpleDebugMenuLogger : DebugTool {
    override val title: String = "Logger"

    private val _logs = mutableStateListOf<Pair<String, String>>()
    val logs: List<String> by derivedStateOf { _logs.map { it.second }.reversed() }

    var isEnabled by mutableStateOf(true)

    @OptIn(ExperimentalUuidApi::class)
    fun log(message: String) {
        if (isEnabled) {
            _logs.add(Uuid.random().toHexString() to message)
        }
    }

    fun clear() {
        _logs.clear()
    }

    @Composable
    override fun Content(context: DebugTool.ContentContext) {
        LazyColumn {
            stickyHeader {
                Row {
                    Text("Logs: ${logs.size}", Modifier.weight(1f))

                    Button(onClick = { _logs.clear() }) {
                        Text("Clear")
                    }
                }
            }

            items(_logs.reversed(), key = { (key, _) -> key }) { (_, message) ->
                Text(
                    text = message,
                    modifier = Modifier
                        .animateItem()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}
