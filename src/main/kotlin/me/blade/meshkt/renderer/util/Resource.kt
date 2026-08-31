package me.blade.meshkt.renderer.util

import kotlin.jvm.javaClass

fun Any.resourceText(path: String): String {
    val stream = this.javaClass.getResourceAsStream(path)!!
    return stream.bufferedReader(Charsets.UTF_8).readText()
}