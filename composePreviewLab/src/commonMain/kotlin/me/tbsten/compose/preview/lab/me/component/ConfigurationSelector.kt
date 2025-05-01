package me.tbsten.compose.preview.lab.me.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import me.tbsten.compose.preview.lab.me.PreviewLabConfiguration

// TODO configuration を Dropdown Menu で選べるようにする
// TODO ConfigurationSelector -> PreviewLabHeader
@Composable
internal fun ConfigurationSelector(
    configurations: List<PreviewLabConfiguration>,
    modifier: Modifier = Modifier,
    content: @Composable (PreviewLabConfiguration) -> Unit
) {
    var selectedIndex by remember { mutableStateOf(0) }
    var openDetail by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (configurations.size >= 2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    configurations.forEachIndexed { index, conf ->
                        if (selectedIndex == index) {
                            OutlinedButton(
                                onClick = {
                                    openDetail = !openDetail
                                },
                            ) {
                                Text(conf.name)
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    selectedIndex = index
                                },
                            ) {
                                Text(conf.name)
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = openDetail,
                ) {
                    val selectedConf = configurations[selectedIndex]
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("maxWidth: ${selectedConf.maxWidth}")
                        Text("maxHeight: ${selectedConf.maxHeight}")
                    }
                }
                HorizontalDivider()
            }
        }
        Crossfade(
            targetState = selectedIndex,
            modifier = Modifier
                .zIndex(-1f)
        ) { selectedIndex ->
            content(configurations[selectedIndex])
        }
    }
}
