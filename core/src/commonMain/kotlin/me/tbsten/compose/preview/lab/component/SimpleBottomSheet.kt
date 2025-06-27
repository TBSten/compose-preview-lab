package me.tbsten.compose.preview.lab.component

import androidx.compose.runtime.Composable

@Composable
internal fun SimpleBottomSheet(isVisible: Boolean, onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
//    val sheetState = rememberModalBottomSheetState(
//        initialDetent = SheetDetent.Hidden,
//        animationSpec = tween(200),
//        detents = listOf(SheetDetent.Hidden, SheetDetent.Open),
//    ).also { state ->
//        LaunchedEffect(isVisible) {
//            if (isVisible) {
//                state.animateTo(SheetDetent.Open)
//            } else {
//                state.animateTo(SheetDetent.Hidden)
//            }
//        }
//    }
//
//    ModalBottomSheet(state = sheetState, onDismiss = onDismissRequest) {
//        Scrim(
//            enter = fadeIn(tween(200)),
//            exit = fadeOut(tween(150)),
//        )
//        Sheet(
//            backgroundColor = PreviewLabTheme.colors.background,
//            contentColor = PreviewLabTheme.colors.onBackground,
//        ) {
//            content()
//        }
//    }
}
