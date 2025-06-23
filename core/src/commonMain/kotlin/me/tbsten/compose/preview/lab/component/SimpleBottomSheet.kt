package me.tbsten.compose.preview.lab.component

import androidx.compose.runtime.Composable
import com.nomanr.composables.bottomsheet.rememberModalBottomSheetState
import me.tbsten.compose.preview.lab.ui.components.ModalBottomSheet

@Composable
internal fun SimpleBottomSheet(isVisible: Boolean, onDismissRequest: () -> Unit, content: @Composable () -> Unit,) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        isVisible = isVisible,
        onDismissRequest = { onDismissRequest() },
        sheetState = bottomSheetState,
    ) {
        content()
    }
}
