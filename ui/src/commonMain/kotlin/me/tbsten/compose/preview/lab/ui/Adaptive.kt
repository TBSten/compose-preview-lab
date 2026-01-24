package me.tbsten.compose.preview.lab.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.tbsten.compose.preview.lab.UiComposePreviewLabApi

@UiComposePreviewLabApi
@Composable
fun <T> adaptive(small: T, medium: T = small, large: T = medium): T = adaptive(
    small = { small },
    medium = { medium },
    large = { large },
)

@UiComposePreviewLabApi
@Composable
fun <T> adaptive(
    small: @Composable () -> T,
    medium: @Composable () -> T = small,
    large: @Composable () -> T = medium,
    @Suppress("unused") disableTrailingLambda: Nothing? = null,
): T {
    val breakpoint = currentBreakpoint()
    return when (breakpoint) {
        Breakpoint.Small -> small()
        Breakpoint.Medium -> medium()
        Breakpoint.Large -> large()
    }
}

@UiComposePreviewLabApi
@Composable
fun currentBreakpoint(): Breakpoint {
    val screenWidth =
        with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.width.toDp()
        }
    return Breakpoint.fromWidth(screenWidth)
}

@UiComposePreviewLabApi
enum class Breakpoint(val maxWidth: Dp) {
    Small(600.dp),
    Medium(1200.dp),
    Large(Dp.Infinity),
    ;

    operator fun <T> invoke(block: () -> T) = DataOverBreakpoint(this, block)

    @UiComposePreviewLabApi
    inner class DataOverBreakpoint<T> internal constructor(val breakpoint: Breakpoint, val data: () -> T)

    @UiComposePreviewLabApi
    companion object {
        fun fromWidth(width: Dp) = entries.firstOrNull {
            width <= it.maxWidth
        } ?: entries.last()
    }
}

@Composable
@UiComposePreviewLabApi
fun AdaptiveContainer(
    small: @Composable (content: @Composable () -> Unit) -> Unit,
    medium: @Composable (content: @Composable () -> Unit) -> Unit = small,
    large: @Composable (content: @Composable () -> Unit) -> Unit = medium,
    content: @Composable () -> Unit,
) = adaptive(
    small = small,
    medium = medium,
    large = large,
).let {
    it.invoke { content() }
}

@Composable
@UiComposePreviewLabApi
fun <S> AdaptiveContainer(
    small: @Composable (content: @Composable S.() -> Unit) -> Unit,
    medium: @Composable (content: @Composable S.() -> Unit) -> Unit = small,
    large: @Composable (content: @Composable S.() -> Unit) -> Unit = medium,
    content: @Composable S.() -> Unit,
) = adaptive(
    small = small,
    medium = medium,
    large = large,
).let {
    it.invoke { content() }
}

@Composable
@UiComposePreviewLabApi
fun <A, R> adaptive(arg: A, small: (A) -> R, medium: (A) -> R = small, large: (A) -> R = medium): R = adaptive(
    small = { small(arg) },
    medium = { medium(arg) },
    large = { large(arg) },
)

@Composable
@UiComposePreviewLabApi
fun <A, B, R> adaptive(arg1: A, arg2: B, small: (A, B) -> R, medium: (A, B) -> R = small, large: (A, B) -> R = medium): R =
    adaptive(
        small = { small(arg1, arg2) },
        medium = { medium(arg1, arg2) },
        large = { large(arg1, arg2) },
    )

@Composable
@UiComposePreviewLabApi
fun <A, B, C, R> adaptive(
    arg1: A,
    arg2: B,
    arg3: C,
    small: (A, B, C) -> R,
    medium: (A, B, C) -> R = small,
    large: (A, B, C) -> R = medium,
): R = adaptive(
    small = { small(arg1, arg2, arg3) },
    medium = { medium(arg1, arg2, arg3) },
    large = { large(arg1, arg2, arg3) },
)

@UiComposePreviewLabApi
object AdaptiveWithReceiver

@Composable
@UiComposePreviewLabApi
fun <T, R> T.adaptive(
    small: T.() -> R,
    medium: T.() -> R = small,
    large: T.() -> R = medium,
    marker: AdaptiveWithReceiver,
): R {
    val receiver = this
    return adaptive(
        small = { receiver.small() },
        medium = { receiver.medium() },
        large = { receiver.large() },
    )
}
