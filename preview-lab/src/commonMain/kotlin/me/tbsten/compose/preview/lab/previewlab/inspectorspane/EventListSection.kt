package me.tbsten.compose.preview.lab.previewlab.inspectorspane

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.tbsten.compose.preview.lab.PreviewLabEvent
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.components.CommonIconButton
import me.tbsten.compose.preview.lab.ui.components.CommonListHeader
import me.tbsten.compose.preview.lab.ui.components.Divider
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.generated.resources.PreviewLabUiRes
import me.tbsten.compose.preview.lab.ui.generated.resources.icon_delete
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalTime::class)
@Composable
internal fun EventListSection(events: List<PreviewLabEvent>, selectedEvent: PreviewLabEvent?, onClear: () -> Unit) {
    SelectionContainer {
        LazyColumn {
            stickyHeader {
                CommonListHeader(
                    title = "${events.size} items",
                    actions = {
                        // TODO add export button
                        CommonIconButton(
                            painter = painterResource(PreviewLabUiRes.drawable.icon_delete),
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
                    val coroutineScope = rememberCoroutineScope()
                    var showDetail by remember { mutableStateOf(false) }
                    var now by remember { mutableStateOf(Clock.System.now().epochSeconds) }
                    var isHighlighted by remember { mutableStateOf(false) }

                    val highlightColor by animateColorAsState(
                        targetValue = if (isHighlighted) {
                            PreviewLabTheme.colors.primary.copy(alpha = 0.2f)
                        } else {
                            Color.Transparent
                        },
                        animationSpec = tween(durationMillis = 300),
                        label = "highlight",
                    )

                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(1.seconds)
                            now = Clock.System.now().epochSeconds
                        }
                    }
                    LaunchedEffect(selectedEvent == event) {
                        if (selectedEvent == event) {
                            isHighlighted = true
                            showDetail = true
                            delay(800.milliseconds)
                            isHighlighted = false
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(highlightColor)
                            .clickable {
                                showDetail = !showDetail
                                coroutineScope.launch {
                                    isHighlighted = true
                                    delay(800.milliseconds)
                                    isHighlighted = false
                                }
                            }
                            .padding(12.dp)
                            .fillMaxWidth()
                            .animateItem(),
                    ) {
                        Text(
                            text = event.title,
                            fontWeight = FontWeight.Bold,
                            style = PreviewLabTheme.typography.body2,
                        )
                        Text(
                            text = "${now - event.createAt.epochSeconds} seconds ago",
                            style = PreviewLabTheme.typography.body2,
                        )
                    }

                    AnimatedVisibility(
                        visible = showDetail,
                        modifier = Modifier,
                    ) {
                        Text(
                            text = event.description ?: "No Detail",
                            style = PreviewLabTheme.typography.body2,
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
            style = PreviewLabTheme.typography.h2,
        )
        Text(
            text = "No events have been issued by onEvent(\"title\") yet.",
            style = PreviewLabTheme.typography.body2,
        )
    }
}
