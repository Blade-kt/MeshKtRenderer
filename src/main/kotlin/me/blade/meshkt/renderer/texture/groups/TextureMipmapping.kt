package me.blade.meshkt.renderer.texture.groups

import me.blade.meshkt.renderer.dsa.GLProperty.Companion.property
import me.blade.meshkt.renderer.texture.TextureHandle
import org.lwjgl.opengl.GL45C.*

class TextureMipmapping(private val handle: TextureHandle) {
    var baseLevel by property(0) { value ->
        glTextureParameteri(handle.gl, GL_TEXTURE_BASE_LEVEL, value)
    }

    var maxLevel by property(1000) { value ->
        glTextureParameteri(handle.gl, GL_TEXTURE_MAX_LEVEL, value)
    }

    var minLOD by property(-1000) { value ->
        glTextureParameteri(handle.gl, GL_TEXTURE_MIN_LOD, value)
    }

    var maxLOD by property(1000) { value ->
        glTextureParameteri(handle.gl, GL_TEXTURE_MAX_LOD, value)
    }

    fun generate() {
        glGenerateTextureMipmap(handle.gl)
    }
}