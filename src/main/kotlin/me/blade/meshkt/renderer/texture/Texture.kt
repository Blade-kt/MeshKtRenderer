package me.blade.meshkt.renderer.texture

import me.blade.meshkt.renderer.MeshEngine
import me.blade.meshkt.renderer.dsa.GLProperty
import me.blade.meshkt.renderer.dsa.GLProperty.Companion.property
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.resource.MeshSyncContext
import me.blade.meshkt.renderer.texture.groups.TextureFiltering
import me.blade.meshkt.renderer.texture.groups.TextureMipmapping
import me.blade.meshkt.renderer.texture.groups.TextureStorage
import me.blade.meshkt.renderer.texture.groups.TextureWrapping
import me.blade.meshkt.renderer.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.texture.properties.TextureMinFilter
import me.blade.meshkt.renderer.texture.properties.TextureTarget
import me.blade.meshkt.renderer.texture.properties.TextureWrap
import org.lwjgl.opengl.GL45C.*

@MeshSyncContext
class Texture(val target: TextureTarget) : IMeshResource {
    val handle = TextureHandle(target)

    val filtering = TextureFiltering(handle)
    val wrapping = TextureWrapping(handle)
    val mipmap = TextureMipmapping(handle)

    val storage = TextureStorage(this)

    override fun free() {
        handle.free()
    }

    companion object {
        fun MeshEngine.createTexture(
            target: TextureTarget = TextureTarget.Texture2D,
            block: Texture.() -> Unit = {}
        ): Texture {
            val engine = this

            val texture = Texture(target)
            engine.resourceFactory.registerResource(texture)

            block(texture)
            return texture
        }
    }
}