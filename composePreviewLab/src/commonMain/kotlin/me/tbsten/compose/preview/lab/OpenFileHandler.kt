package me.tbsten.compose.preview.lab

import androidx.compose.runtime.compositionLocalOf

fun interface OpenFileHandler {
    fun openFile(filePathInProject: String)
    operator fun invoke(filePathInProject: String) = openFile(filePathInProject)
}

val LocalOpenFileHandler = compositionLocalOf<OpenFileHandler?> { null }
