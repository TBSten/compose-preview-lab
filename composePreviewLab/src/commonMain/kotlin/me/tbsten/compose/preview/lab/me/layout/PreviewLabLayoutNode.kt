package me.tbsten.compose.preview.lab.me.layout

import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import kotlin.random.Random

internal class PreviewLabLayoutNode(
    private val id: Long = Random.nextLong(),
    val label: String,
    val offsetInAppRoot: DpOffset?,
    val size: DpSize?,
    val resizable: Boolean,
) {
    fun offsetInContentRoot(contentRootOffset: DpOffset): DpOffset? = TODO("offsetInContentRoot")

    override operator fun equals(other: Any?) =
        other is PreviewLabLayoutNode &&
                other.id == this.id

    override fun hashCode(): Int = this.id.hashCode()
}
