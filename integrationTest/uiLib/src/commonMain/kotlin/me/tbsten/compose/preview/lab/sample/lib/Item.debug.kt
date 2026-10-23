package me.tbsten.compose.preview.lab.sample.lib

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import me.tbsten.compose.preview.lab.extension.debugger.debugtool.debuggable
import me.tbsten.compose.preview.lab.extension.debugger.debugtool.debuggableBasic
import me.tbsten.compose.preview.lab.sample.debugmenu.AppDebugMenu
import me.tbsten.compose.preview.lab.sample.debugmenu.GetItemListUseCaseDebugBehavior
import me.tbsten.compose.preview.lab.sample.debugmenu.GetItemListUseCaseDebugBehaviorReturnValueType

class DebuggableGetItemListUseCase(private val default: GetItemListUseCase) : GetItemListUseCase {
    override suspend fun invoke(): List<Item> = suspend { default.invoke() }
        .debuggable(AppDebugMenu.useCases.getItemListUseCaseDebugBehavior) { debugBehavior ->
            delay(debugBehavior.delay)

            when (debugBehavior.result) {
                GetItemListUseCaseDebugBehavior.Result.Default -> runDefault()
                GetItemListUseCaseDebugBehavior.Result.ReturnFakeNormal -> listOf(Item("Fake1"), Item("Fake2"))
                GetItemListUseCaseDebugBehavior.Result.ReturnFakeEmpty -> emptyList()
                GetItemListUseCaseDebugBehavior.Result.Error -> error("Error from debug menu")
                GetItemListUseCaseDebugBehavior.Result.Cancel -> throw CancellationException("Cancel from debug menu")
            }
        }
        .debuggableBasic(AppDebugMenu.useCases.getItemListUseCaseDebugBehavior2) { resultType ->
            when (resultType) {
                GetItemListUseCaseDebugBehaviorReturnValueType.Normal -> listOf(Item("Fake1"), Item("Fake2"))
                GetItemListUseCaseDebugBehaviorReturnValueType.Empty -> emptyList()
            }
        }
        .invoke()
}

class DebuggableUpdateItemUseCase(private val default: UpdateItemUseCase) : UpdateItemUseCase {
    override suspend fun invoke(id: String, item: Item) = suspend { default.invoke(id, item) }
        .debuggableBasic(AppDebugMenu.useCases.updateItemUseDebugBehavior)
        .invoke()
}
