package me.tbsten.compose.preview.lab.sample.debugmenu

import me.tbsten.compose.preview.lab.extension.debugger.DebugMenu
import me.tbsten.compose.preview.lab.extension.debugger.DebugToolGroup
import me.tbsten.compose.preview.lab.extension.debugger.debugtool.SimpleLogger
import me.tbsten.compose.preview.lab.extension.debugger.debugtool.basicFunctionDebugBehavior
import me.tbsten.compose.preview.lab.field.EnumField

object AppDebugMenu : DebugMenu() {
    val logger = tool { SimpleLogger() }

    val useCases = tool {
        UseCasesDebugBehaviors()
    }
}

class UseCasesDebugBehaviors : DebugToolGroup("UseCases") {
    val getItemListUseCaseDebugBehavior = tool {
        getItemListUseCaseDebugBehavior()
    }

    val getItemListUseCaseDebugBehavior2 = tool {
        basicFunctionDebugBehavior(
            label = "getItemListUseCaseDebugBehavior2",
            returnValueField = EnumField<GetItemListUseCaseDebugBehaviorReturnValueType>(
                "Result",
                GetItemListUseCaseDebugBehaviorReturnValueType.Normal,
            ),
        )
    }

    val updateItemUseDebugBehavior = tool {
        basicFunctionDebugBehavior(
            label = "updateItemUseDebugBehavior",
        )
    }
}

enum class GetItemListUseCaseDebugBehaviorReturnValueType {
    Normal,
    Empty,
}
