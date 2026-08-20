package me.blade.meshkt.renderer.texture

import me.blade.meshkt.renderer.MeshEngine
import me.blade.meshkt.renderer.resource.MeshResource
import me.blade.meshkt.renderer.resource.MeshDef3rredContext
import me.blade.meshkt.renderer.resource.MeshSyncContext
import org.lwjgl.opengl.GL45C.*

@MeshSyncContext
class Texture(
    descriptor: Descriptor
) : MeshResource() {
    @MeshDef3rredContext
    class Descriptor {
        var target: TextureTarget? = null

        var minFilter: TextureMinFilter? = null
        var magFilter: TextureMagFilter? = null
    }

    private var _handle: Int? = null
    val handle get() = _handle ?: throw IllegalStateException("Texture is not initialized yet")

    var minFilter = TextureMinFilter.NearestMipmapLinear; set(value) {
        if (field == value) return
        field = value

        glTextureParameteri(handle, GL_TEXTURE_MIN_FILTER, value.gl)
    }

    var magFilter = TextureMagFilter.Linear; set(value) {
        if (field == value) return
        field = value

        glTextureParameteri(handle, GL_TEXTURE_MAG_FILTER, value.gl)
    }

    init {
        val target = descriptor.target ?: throw IllegalStateException("Descriptor target is null")
        _handle = glCreateTextures(target.gl)

        minFilter = descriptor.minFilter ?: minFilter
        magFilter = descriptor.magFilter ?: magFilter
    }

    override fun free() {
        glDeleteTextures(handle)
    }

    companion object {
        fun MeshEngine.createTexture(block: Descriptor.() -> Unit): Texture {
            val engine = this

            val descriptor = Descriptor()
            block(descriptor)

            val texture = Texture(descriptor)
            engine.resourceFactory.registerResource(texture)
            return texture
        }
    }
}