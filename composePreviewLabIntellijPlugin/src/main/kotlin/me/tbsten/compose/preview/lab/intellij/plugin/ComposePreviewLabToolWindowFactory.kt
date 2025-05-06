package me.tbsten.compose.preview.lab.intellij.plugin

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import me.tbsten.compose.preview.lab.me.CollectedPreview
import me.tbsten.compose.preview.lab.me.PreviewLab
import me.tbsten.compose.preview.lab.me.PreviewLabRoot
import me.tbsten.compose.preview.lab.me.field.StringField
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.ui.component.Text

class ComposePreviewLabToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        val selectedFileFlow = run {
            val flow = MutableSharedFlow<VirtualFile>()
            project.messageBus.connect().subscribe(
                FileEditorManagerListener.FILE_EDITOR_MANAGER,
                object : FileEditorManagerListener {
                    override fun selectionChanged(event: FileEditorManagerEvent) {
                        event.newFile?.let { flow.tryEmit(it) }
                    }
                }
            )
            flow
        }

        toolWindow.addComposeTab {
            val selectedFileFlow by selectedFileFlow.collectAsState(null)
            Text("selectedFileFlow: ${selectedFileFlow?.name ?: "null"}")
            PreviewLabRoot(
                // TODO capture from ksp generated source
                previews = List(3) {
                    CollectedPreview("Preview $it") {
                        PreviewLab {
                            Button(
                                onClick = { onEvent("Click $it") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minWidth = 100.dp)
                            ) {
                                Text(fieldValue { StringField("value", "Preview $it") })
                            }
                        }
                    }
                },
            )
        }
    }
}
