package me.tbsten.compose.preview.lab.component

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import com.composables.core.ModalBottomSheet
import com.composables.core.Scrim
import com.composables.core.Sheet
import com.composables.core.SheetDetent
import com.composables.core.rememberModalBottomSheetState
import me.tbsten.compose.preview.lab.ui.PreviewLabTheme

private val SheetDetent.Companion.Open by lazy {
    SheetDetent("open") { containerHeight, _ -> containerHeight - 40.dp }
}

@Composable
internal fun SimpleBottomSheet(isVisible: Boolean, onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    val sheetState = rememberModalBottomSheetState(
        initialDetent = SheetDetent.Hidden,
        animationSpec = tween(200),
        detents = listOf(SheetDetent.Hidden, SheetDetent.Open),
    ).also { state ->
        LaunchedEffect(isVisible) {
            if (isVisible) {
                state.animateTo(SheetDetent.Open)
            } else {
                state.animateTo(SheetDetent.Hidden)
            }
        }
    }

    ModalBottomSheet(state = sheetState, onDismiss = onDismissRequest) {
        Scrim(
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150)),
        )
        Sheet(
            backgroundColor = PreviewLabTheme.colors.background,
            contentColor = PreviewLabTheme.colors.onBackground,
        ) {
            content()
        }
    }
}
