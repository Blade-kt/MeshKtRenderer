package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.engine.MeshEngine
import me.blade.meshkt.renderer.objects.texture.Texture.Companion.createTexture
import me.blade.meshkt.renderer.objects.texture.Texture2DHandle
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot

object MeshRenderer {
    val engine = MeshEngine.create()

    init {
        engine.dispatcher.launch {
            engine.state.boundTextures[TextureSlot.Slot0] = engine.createTexture(
                Texture2DHandle.create()
            )
        }
    }
}