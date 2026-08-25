package me.blade.meshkt.renderer.threading

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

fun <T> Deferred<T>.getOrNull(): T? {
    if (!isCompleted) return null
    @OptIn(ExperimentalCoroutinesApi::class)
    return getCompleted()
}

fun <T> Deferred<T>.runIfCompleted(block: (T) -> Unit) {
    getOrNull()?.let { block(it) }
}