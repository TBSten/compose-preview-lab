package me.tbsten.compose.preview.lab.sample.lib

import kotlin.jvm.JvmInline
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@JvmInline
value class Item(val value: String)

interface GetItemListUseCase {
    suspend operator fun invoke(): List<Item>
}

class GetItemListUseCaseImpl : GetItemListUseCase {
    override suspend operator fun invoke(): List<Item> {
        // TODO replace with actual implementation by api
        return listOf(Item("item1"), Item("item2"), Item("item3"))
    }
}

interface UpdateItemUseCase {
    suspend operator fun invoke(id: String, item: Item)
}

class UpdateItemUseCaseImpl : UpdateItemUseCase {
    override suspend fun invoke(id: String, item: Item) {
        // TODO replace with actual implementation by api
        delay(2.seconds)
    }
}
