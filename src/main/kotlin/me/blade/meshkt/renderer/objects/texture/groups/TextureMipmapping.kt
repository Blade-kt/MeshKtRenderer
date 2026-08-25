package me.blade.meshkt.renderer.objects.texture.groups

import me.blade.meshkt.renderer.objects.texture.TextureHandle
import me.blade.meshkt.renderer.util.GLProperty.Companion.property
import org.lwjgl.opengl.GL45C.*

class TextureMipmapping(private val handle: TextureHandle) {
    var baseLevel by property(0) { value ->
        glTextureParameteri(handle.id, GL_TEXTURE_BASE_LEVEL, value)
    }

    var maxLevel by property(1000) { value ->
        glTextureParameteri(handle.id, GL_TEXTURE_MAX_LEVEL, value)
    }

    var minLOD by property(-1000) { value ->
        glTextureParameteri(handle.id, GL_TEXTURE_MIN_LOD, value)
    }

    var maxLOD by property(1000) { value ->
        glTextureParameteri(handle.id, GL_TEXTURE_MAX_LOD, value)
    }

    fun generate() {
        glGenerateTextureMipmap(handle.id)
    }
}