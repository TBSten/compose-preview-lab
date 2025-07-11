package me.tbsten.compose.preview.lab.sample

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import compose_preview_lab_integration_test.app.generated.resources.IndieFlower_Regular
import compose_preview_lab_integration_test.app.generated.resources.Res
import compose_preview_lab_integration_test.app.generated.resources.cyclone
import compose_preview_lab_integration_test.app.generated.resources.ic_cyclone
import compose_preview_lab_integration_test.app.generated.resources.ic_dark_mode
import compose_preview_lab_integration_test.app.generated.resources.ic_light_mode
import compose_preview_lab_integration_test.app.generated.resources.ic_rotate_right
import compose_preview_lab_integration_test.app.generated.resources.open_github
import compose_preview_lab_integration_test.app.generated.resources.run
import compose_preview_lab_integration_test.app.generated.resources.stop
import compose_preview_lab_integration_test.app.generated.resources.theme
import kotlinx.coroutines.isActive
import me.tbsten.compose.preview.lab.ComposePreviewLabOption
import me.tbsten.compose.preview.lab.PreviewLab
import me.tbsten.compose.preview.lab.sample.theme.AppTheme
import me.tbsten.compose.preview.lab.sample.theme.LocalThemeIsDark
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// ref: https://terrakok.github.io/Compose-Multiplatform-Wizard/
@Composable
internal fun ComposeMultiplatformWizardDefaultUI() = AppTheme {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TestPublicPreview()
        HorizontalDivider()
        Text(
            text = stringResource(Res.string.cyclone),
            fontFamily = FontFamily(Font(Res.font.IndieFlower_Regular)),
            style = MaterialTheme.typography.displayLarge,
        )

        var isRotating by remember { mutableStateOf(false) }

        val rotate = remember { Animatable(0f) }
        val target = 360f
        if (isRotating) {
            LaunchedEffect(Unit) {
                while (isActive) {
                    val remaining = (target - rotate.value) / target
                    rotate.animateTo(
                        target,
                        animationSpec = tween((1_000 * remaining).toInt(), easing = LinearEasing),
                    )
                    rotate.snapTo(0f)
                }
            }
        }

        Image(
            painter = painterResource(Res.drawable.ic_cyclone),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .size(250.dp)
                .padding(16.dp)
                .run { rotate(rotate.value) },
        )

        ElevatedButton(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .widthIn(min = 200.dp),
            onClick = { isRotating = !isRotating },
            content = {
                Icon(
                    painter = painterResource(Res.drawable.ic_rotate_right),
                    contentDescription = null,
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    stringResource(if (isRotating) Res.string.stop else Res.string.run),
                )
            },
        )

        var isDark by LocalThemeIsDark.current
        val icon = remember(isDark) {
            if (isDark) {
                Res.drawable.ic_light_mode
            } else {
                Res.drawable.ic_dark_mode
            }
        }

        ElevatedButton(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 200.dp),
            onClick = { isDark = !isDark },
            content = {
                Icon(painterResource(icon), contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(Res.string.theme))
            },
        )

        val uriHandler = LocalUriHandler.current
        TextButton(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 200.dp),
            onClick = { uriHandler.openUri("https://github.com/terrakok") },
        ) {
            Text(stringResource(Res.string.open_github))
        }
    }
}

@ComposePreviewLabOption(displayName = "Compose Multiplatform Wizard Default UI")
@Preview
@Composable
private fun ComposeMultiplatformWizardDefaultUIPreview() = PreviewLab {
    ComposeMultiplatformWizardDefaultUI()
}
