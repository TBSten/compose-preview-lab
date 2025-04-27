package me.tbsten.compose.preview.lab.me.field

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

abstract class PreviewLabField<Value>(
    initialValue: Value,
) : MutableState<Value> by mutableStateOf(initialValue)
