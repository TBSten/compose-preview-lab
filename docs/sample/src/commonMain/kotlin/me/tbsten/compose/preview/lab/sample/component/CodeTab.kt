package me.tbsten.compose.preview.lab.sample.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tbsten.compose.preview.lab.previewlab.inspectorspane.InspectorTab

class CodeTab(code: String, override val title: String = "Code") : InspectorTab {
    internal var code by mutableStateOf(code)

    @Composable
    override fun InspectorTab.ContentContext.Content() {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .background(createCodeBlockColor(), shape = RoundedCornerShape(8.dp))
                .verticalScroll(rememberScrollState()),
        ) {
            SelectionContainer {
                KotlinCodeBlock(
                    code = code,
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
internal fun rememberCodeTab(code: String) = remember {
    CodeTab(
        code = code,
    )
}.apply { this.code = code }

@Composable
internal fun createCodeBlockColor(): Color = MaterialTheme.colorScheme.surfaceVariant

@Composable
internal fun KotlinCodeBlock(
    code: String,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Text(
        text = code,
        modifier = modifier.padding(contentPadding),
        fontSize = 12.sp,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurface,
    )
}
