package me.tbsten.compose.preview.lab.me

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.me.component.ConfigurationSelector
import me.tbsten.compose.preview.lab.me.component.FieldListSection
import me.tbsten.compose.preview.lab.me.util.thenIfNotNull

@Composable
fun PreviewLab(
    maxWidth: Dp? = null,
    maxHeight: Dp? = null,
    content: @Composable PreviewLabScope.() -> Unit,
) = PreviewLab(
    configurations = listOf(PreviewLabConfiguration.Default),
    content = content,
)

@Composable
fun PreviewLab(
    configurations: List<PreviewLabConfiguration> = listOf(PreviewLabConfiguration.Default),
    content: @Composable PreviewLabScope.() -> Unit,
) {
    check(configurations.isNotEmpty())

    Column {
        ConfigurationSelector(
            configurations = configurations,
        ) { conf ->
            val scope = remember { PreviewLabScope() }

            CompositionLocalProvider(
                LocalPreviewLabScope provides scope,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                        Box(
                            modifier = Modifier
                                .border(
                                    8.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                )
                                .padding(8.dp)
                                .thenIfNotNull(conf.maxHeight) { widthIn(max = it) }
                                .thenIfNotNull(conf.maxWidth) { widthIn(max = it) }
                        ) {
                            content(scope)
                        }
                    }

                    VerticalDivider()
                    Column(
                        modifier = Modifier
                            .widthIn(min = 150.dp, max = 300.dp)
                            .fillMaxHeight()
                            .padding(8.dp)
                    ) {
                        // TODO events, layouts
                        FieldListSection(
                            fields = scope.fields,
                        )
                    }
                }
            }
        }
    }
}

internal val LocalPreviewLabScope = compositionLocalOf<PreviewLabScope?> { null }
