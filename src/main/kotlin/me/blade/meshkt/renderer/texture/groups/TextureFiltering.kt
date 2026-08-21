package me.blade.meshkt.renderer.texture.groups

import me.blade.meshkt.renderer.dsa.GLProperty.Companion.property
import me.blade.meshkt.renderer.texture.TextureHandle
import me.blade.meshkt.renderer.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.texture.properties.TextureMinFilter
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL45C.glTextureParameteri

class TextureFiltering(handle: TextureHandle) {
    var minFilter by property(TextureMinFilter.NearestMipmapLinear) { value ->
        glTextureParameteri(handle.gl, GL_TEXTURE_MIN_FILTER, value.gl)
    }

    var magFilter by property(TextureMagFilter.Linear) { value ->
        glTextureParameteri(handle.gl, GL_TEXTURE_MAG_FILTER, value.gl)
    }
}