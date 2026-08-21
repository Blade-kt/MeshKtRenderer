package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.texture.Texture
import me.blade.meshkt.renderer.texture.Texture.Companion.createTexture
import me.blade.meshkt.renderer.threading.GLThreadDispatcher

object MeshRenderer {
    val dispatcher = GLThreadDispatcher()
    val engine = MeshEngine(dispatcher)

    init {
        dispatcher.launch {
            engine.createTexture {

            }
        }
    }
}