package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.threading.GLThreadDispatcher

object MeshRenderer {
    val dispatcher = GLThreadDispatcher()
    val engine = MeshEngine(dispatcher)

    init {
        dispatcher.launch {

        }
    }
}