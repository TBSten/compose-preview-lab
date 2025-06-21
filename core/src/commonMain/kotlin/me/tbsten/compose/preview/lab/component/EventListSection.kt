package me.tbsten.compose.preview.lab.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.delay
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_delete
import me.tbsten.compose.preview.lab.event.PreviewLabEvent
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalTime::class)
@Composable
internal fun EventListSection(events: List<PreviewLabEvent>, onClear: () -> Unit) {
    SelectionContainer {
        LazyColumn {
            stickyHeader {
                CommonListHeader(
                    title = "${events.size} items",
                    actions = {
                        // TODO add export button
                        CommonIconButton(
                            painter = painterResource(Res.drawable.icon_delete),
                            contentDescription = "Clear Events",
                            enabled = events.isNotEmpty(),
                            onClick = onClear,
                        )
                    },
                )
            }

            if (events.isEmpty()) {
                item("No item") {
                    NoEvents()
                }
            }

            items(events, key = { it.createAt }) { event ->
                Column(modifier = Modifier.animateItem()) {
                    var showDetail by remember { mutableStateOf(false) }
                    var now by remember { mutableStateOf(Clock.System.now().epochSeconds) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1.seconds)
                            now = Clock.System.now().epochSeconds
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clickable { showDetail = !showDetail }
                            .padding(12.dp)
                            .fillMaxWidth()
                            .animateItem(),
                    ) {
                        Text(
                            text = event.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "${now - event.createAt.epochSeconds} seconds ago",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    AnimatedVisibility(
                        visible = showDetail,
                        modifier = Modifier,
                    ) {
                        Text(
                            text = event.description ?: "No Detail",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }

                    Divider()
                }
            }
        }
    }
}

@Composable
private fun NoEvents() {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(
            text = "No Events",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "No events have been issued by onEvent(\"title\") yet.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
