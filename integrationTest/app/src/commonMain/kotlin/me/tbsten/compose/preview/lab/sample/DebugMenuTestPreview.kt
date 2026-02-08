package me.tbsten.compose.preview.lab.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.tbsten.compose.preview.lab.previewlab.PreviewLab
import me.tbsten.compose.preview.lab.sample.debugmenu.AppDebugMenu
import me.tbsten.compose.preview.lab.sample.lib.DebuggableGetItemListUseCase
import me.tbsten.compose.preview.lab.sample.lib.DebuggableUpdateItemUseCase
import me.tbsten.compose.preview.lab.sample.lib.GetItemListUseCase
import me.tbsten.compose.preview.lab.sample.lib.GetItemListUseCaseImpl
import me.tbsten.compose.preview.lab.sample.lib.Item
import me.tbsten.compose.preview.lab.sample.lib.MyButton
import me.tbsten.compose.preview.lab.sample.lib.UpdateItemUseCase
import me.tbsten.compose.preview.lab.sample.lib.UpdateItemUseCaseImpl

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun DebugMenuTestPreview() = PreviewLab {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Debug Menu Test")

        Spacer(Modifier.height(16.dp))

        MyButton(
            text = "Log Now",
            onClick = { AppDebugMenu.logger.info("test ${Clock.System.now()}") },
        )

        Spacer(Modifier.height(16.dp))

        val coroutineScope = rememberCoroutineScope()
        val getItemListUseCase: GetItemListUseCase = remember {
            DebuggableGetItemListUseCase(default = GetItemListUseCaseImpl())
        }
        val updateItemUseCase: UpdateItemUseCase = remember {
            DebuggableUpdateItemUseCase(default = UpdateItemUseCaseImpl())
        }

        MyButton(
            text = "Run GetItemListUseCase",
            onClick = {
                coroutineScope.launch(Dispatchers.Default) {
                    AppDebugMenu.logger.info("Start GetItemListUseCase")

                    try {
                        val result = getItemListUseCase()
                        AppDebugMenu.logger.info("Finish GetItemListUseCase: success: $result")
                    } catch (t: Throwable) {
                        AppDebugMenu.logger.error("Finish GetItemListUseCase: error  :", error = t)
                    }
                }
            },
        )

        MyButton(
            text = "Run UpdateItemUseCase",
            onClick = {
                coroutineScope.launch(Dispatchers.Default) {
                    AppDebugMenu.logger.info("Start UpdateItemUseCase")

                    try {
                        val result = updateItemUseCase("1", Item("Item 1"))
                        AppDebugMenu.logger.info("Finish UpdateItemUseCase: success: $result")
                    } catch (t: Throwable) {
                        AppDebugMenu.logger.error("Finish UpdateItemUseCase: error  :", error = t)
                    }
                }
            },
        )
    }
}

@Preview
@Composable
private fun DebugMenuTestScreenPreview() = PreviewLab {
    Column(Modifier.background(Color.White).fillMaxSize().padding(20.dp)) {
        Text("Debug Menu Test")

        Spacer(Modifier.height(16.dp))

        val coroutineScope = rememberCoroutineScope()
        var itemList by remember { mutableStateOf<List<Item>?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val getItemListUseCase: GetItemListUseCase = remember {
            DebuggableGetItemListUseCase(default = GetItemListUseCaseImpl())
        }

        MyButton(
            text = "Run GetItemListUseCase",
            onClick = {
                coroutineScope.launch(Dispatchers.Default) {
                    AppDebugMenu.logger.info("Start GetItemListUseCase")

                    isLoading = true
                    try {
                        itemList = getItemListUseCase()
                        AppDebugMenu.logger.info("Finish GetItemListUseCase: success: $itemList")
                        errorMessage = null
                    } catch (t: Throwable) {
                        AppDebugMenu.logger.error("Finish GetItemListUseCase: error  :", error = t)
                        errorMessage = t.message
                    } finally {
                        isLoading = false
                    }
                }
            },
        )

        if (isLoading) CircularProgressIndicator()
        if (errorMessage != null) Text("Error: $errorMessage", color = Color.Red)
        if (itemList != null) {
            if (itemList?.isEmpty() == true) {
                Text("itemList is empty", modifier = Modifier.alpha(if (isLoading) 0.25f else 1.00f))
            } else {
                Text("$itemList", modifier = Modifier.alpha(if (isLoading) 0.25f else 1.00f))
            }
        }
    }
}
