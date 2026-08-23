package me.blade.meshkt.renderer.objects.texture

import me.blade.meshkt.renderer.engine.MeshEngine
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.resource.MeshSyncContext
import me.blade.meshkt.renderer.objects.texture.groups.TextureFiltering
import me.blade.meshkt.renderer.objects.texture.groups.TextureMipmapping
import me.blade.meshkt.renderer.objects.texture.groups.TextureStorage
import me.blade.meshkt.renderer.objects.texture.groups.TextureWrapping
import me.blade.meshkt.renderer.objects.texture.TextureHandle
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import org.lwjgl.opengl.GL45C.*

class Texture(val handle: TextureHandle) : IMeshResource {
    val filtering = TextureFiltering(handle)
    val wrapping = TextureWrapping(handle)
    val mipmap = TextureMipmapping(handle)

    val storage = TextureStorage(this)

    override fun free() {
        handle.free()
    }

    companion object {
        @MeshSyncContext
        fun MeshEngine.createTexture(
            handle: TextureHandle,
            block: Texture.() -> Unit = {}
        ): Texture {
            val texture = Texture(handle)
            resourceFactory.registerResource(texture)

            block(texture)
            return texture
        }
    }
}