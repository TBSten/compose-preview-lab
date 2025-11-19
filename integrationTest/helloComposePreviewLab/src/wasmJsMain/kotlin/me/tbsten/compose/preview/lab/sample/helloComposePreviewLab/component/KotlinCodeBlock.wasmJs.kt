package me.tbsten.compose.preview.lab.sample.helloComposePreviewLab.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// FIXME KodeView は wasmJs をサポートしていない
//  ライブラリで wasmJs が対応されたら commonMain で KodeView を呼び出すようにする
@Composable
internal actual fun KotlinCodeBlock(code: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Text(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState()),
            text = code,
        )
    }
}
