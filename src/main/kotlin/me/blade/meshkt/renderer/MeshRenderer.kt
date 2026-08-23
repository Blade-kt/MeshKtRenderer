package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.engine.MeshEngine
import me.blade.meshkt.renderer.objects.texture.Texture.Companion.createTexture
import me.blade.meshkt.renderer.objects.texture.TextureHandle
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.objects.texture.properties.TextureTarget

object MeshRenderer {
    val engine = MeshEngine.create()

    init {
        engine.dispatcher.launch {
            engine.boundTextures[TextureSlot.Slot0] = engine.createTexture(
                TextureHandle.create(TextureTarget.Texture2D)
            )
        }
    }
}