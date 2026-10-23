package me.tbsten.compose.preview.lab.action.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import me.tbsten.compose.preview.lab.ExperimentalComposePreviewLabApi
import me.tbsten.compose.preview.lab.PreviewLabAction
import me.tbsten.compose.preview.lab.PreviewLabField
import me.tbsten.compose.preview.lab.action.defaultResultsViewField

@ExperimentalComposePreviewLabApi
@Composable
internal fun <R> PreviewLabAction<R>.LatestResultView(
    latestStatus: PreviewLabAction.DoActionStatus<R>,
    field: PreviewLabAction<R>.(R) -> PreviewLabField<R>? = defaultResultsViewField(),
) {
    Column {
        when (latestStatus) {
            is PreviewLabAction.DoActionStatus.Running<*> ->
                RunningView()
            is PreviewLabAction.DoActionStatus.Done<*> -> latestStatus.result.fold(
                onSuccess = { result -> SuccessView(result = result, field = field) },
                onFailure = { FailureView(error = it) },
            )
        }
    }
}
