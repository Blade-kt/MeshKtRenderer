package me.blade.meshkt.renderer.objects.texture

import me.blade.meshkt.renderer.engine.MeshEngine
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.objects.texture.groups.TextureFiltering
import me.blade.meshkt.renderer.objects.texture.groups.TextureMipmapping
import me.blade.meshkt.renderer.objects.texture.groups.TextureStorage
import me.blade.meshkt.renderer.objects.texture.groups.TextureWrapping
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot

class Texture(
    val engine: MeshEngine,
    val handle: Texture2DHandle
) : IMeshResource {
    val filtering = TextureFiltering()
    val wrapping = TextureWrapping()
    val mipmap = TextureMipmapping()

    val storage = TextureStorage(this)

    fun use(slot: TextureSlot = TextureSlot.Slot0, block: Texture.() -> Unit) {
        val prev = engine.state.boundTextures[slot]
        engine.state.boundTextures[slot] = this
        block()
        engine.state.boundTextures[slot] = prev
    }

    override fun free() {
        handle.free()
    }

    companion object {
        fun MeshEngine.createTexture(
            handle: Texture2DHandle,
            block: Texture.() -> Unit = {}
        ): Texture {
            val texture = Texture(this, handle)
            resources.registerResource(texture)
            texture.use(TextureSlot.Slot0, block)
            return texture
        }
    }
}