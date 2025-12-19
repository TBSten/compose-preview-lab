package me.tbsten.compose.preview.lab.action.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.PreviewLabAction
import me.tbsten.compose.preview.lab.PreviewLabActionDoActionKey
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.core.generated.resources.Res
import me.tbsten.compose.preview.lab.core.generated.resources.icon_error
import me.tbsten.compose.preview.lab.core.generated.resources.icon_sync
import me.tbsten.compose.preview.lab.core.generated.resources.icon_task_alt
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme
import me.tbsten.compose.preview.lab.ui.adaptive
import me.tbsten.compose.preview.lab.ui.components.CommonListItem
import me.tbsten.compose.preview.lab.ui.components.HorizontalDivider
import me.tbsten.compose.preview.lab.ui.components.Icon
import me.tbsten.compose.preview.lab.ui.components.Surface
import me.tbsten.compose.preview.lab.ui.components.Text
import me.tbsten.compose.preview.lab.ui.components.VerticalDivider
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun <R> PreviewLabAction<R>.DetailDialogContent(
    doActionStatusList: Map<PreviewLabActionDoActionKey, PreviewLabAction.DoActionStatus<R>>,
    field: PreviewLabAction<R>.(R) -> PreviewLabField<R>?,
) {
    val contentPadding = adaptive(12.dp, 20.dp)
    var selectedKey by remember { mutableStateOf<PreviewLabActionDoActionKey?>(doActionStatusList.keys.firstOrNull()) }
    val selected by remember(doActionStatusList) { derivedStateOf { doActionStatusList[selectedKey] } }

    Surface(color = PreviewLabTheme.colors.background, contentColor = PreviewLabTheme.colors.onBackground) {
        Column(Modifier.widthIn(adaptive(0.dp, 800.dp))) {
            Text(
                text = label,
                style = PreviewLabTheme.typography.h2,
                modifier = Modifier.padding(contentPadding),
            )

            HorizontalDivider(thickness = 2.dp)

            Row {
                StatusList(
                    doActionStatusList = doActionStatusList,
                    selected = selected,
                    onSelectChange = { selectedKey = it },
                )
                VerticalDivider()
                StatusDetailPane(
                    selected = selected,
                    field = field,
                )
            }
        }
    }
}

@Composable
private fun <R> StatusList(
    doActionStatusList: Map<PreviewLabActionDoActionKey, PreviewLabAction.DoActionStatus<R>>,
    selected: PreviewLabAction.DoActionStatus<R>?,
    onSelectChange: (PreviewLabActionDoActionKey?) -> Unit,
) {
    LazyColumn(Modifier.wrapContentWidth().widthIn(max = adaptive(120.dp))) {
        items(doActionStatusList.entries.toList()) { (key, doActionStatus) ->
            CommonListItem(
                title = when (doActionStatus) {
                    is PreviewLabAction.DoActionStatus.Done<*> -> doActionStatus.result.fold(
                        onSuccess = { "Success" },
                        onFailure = { "Error" },
                    )
                    is PreviewLabAction.DoActionStatus.Running<*> -> "Running"
                },
                isSelected = selected == doActionStatus,
                onSelect = {
                    if (selected == doActionStatus) {
                        onSelectChange(null)
                    } else {
                        onSelectChange(key)
                    }
                },
            )
        }
    }
}

@Composable
private fun <R> PreviewLabAction<R>.StatusDetailPane(
    selected: PreviewLabAction.DoActionStatus<R>?,
    field: PreviewLabAction<R>.(R) -> PreviewLabField<R>?,
) {
    AnimatedContent(
        targetState = selected,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { currentSelected ->
        Box(Modifier.fillMaxSize()) {
            if (currentSelected != null) {
                LazyColumn {
                    item {
                        StatusSection(selected = currentSelected)
                    }
                    item {
                        HorizontalDivider()
                    }
                    item {
                        ResultSection(
                            selected = currentSelected,
                            field = field,
                            modifier = Modifier.padding(bottom = 20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <R> PreviewLabAction<R>.ResultSection(
    selected: PreviewLabAction.DoActionStatus<R>,
    field: PreviewLabAction<R>.(R) -> PreviewLabField<R>?,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = "Result:",
            modifier = Modifier.padding(horizontal = 8.dp).padding(top = 8.dp),
            style = PreviewLabTheme.typography.h3,
        )
        when (selected) {
            is PreviewLabAction.DoActionStatus.Done<*> -> selected.result.fold(
                onSuccess = { SuccessView(result = it, field = field) },
                onFailure = { FailureView(error = it, showStacktrace = true) },
            )
            is PreviewLabAction.DoActionStatus.Running<*> ->
                RunningView()
        }
    }
}

@Composable
private fun <R> StatusSection(selected: PreviewLabAction.DoActionStatus<R>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(all = 8.dp),
    ) {
        Text(
            text = "Status:",
            style = PreviewLabTheme.typography.h3,
        )

        val (statusText, statusColor, icon) =
            when (selected) {
                is PreviewLabAction.DoActionStatus.Done<*> -> selected.result.fold(
                    onSuccess = {
                        Triple(
                            "Success",
                            PreviewLabTheme.colors.success,
                            Res.drawable.icon_task_alt,
                        )
                    },
                    onFailure = {
                        Triple(
                            "Error",
                            PreviewLabTheme.colors.error,
                            Res.drawable.icon_error,
                        )
                    },
                )
                is PreviewLabAction.DoActionStatus.Running<*> ->
                    Triple(
                        "Running",
                        PreviewLabTheme.colors.textSecondary,
                        Res.drawable.icon_sync,
                    )
            }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = statusText,
                style = PreviewLabTheme.typography.body1,
                color = statusColor,
                modifier = Modifier,
            )
        }

        if (selected is PreviewLabAction.DoActionStatus.Done<*>) {
            Text(
                text = "took ${selected.duration.inWholeMilliseconds} milli seconds",
                style = PreviewLabTheme.typography.body1,
            )
        }
    }
}
