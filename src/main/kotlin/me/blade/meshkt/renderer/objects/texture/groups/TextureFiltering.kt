package me.blade.meshkt.renderer.objects.texture.groups

import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.util.GLProperty.Companion.property
import me.blade.meshkt.renderer.objects.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.objects.texture.properties.TextureMinFilter
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL45C.glTextureParameteri

class TextureFiltering(texture: Texture) {
    var minFilter by property(TextureMinFilter.NearestMipmapLinear) { value ->
        glTextureParameteri(texture.id, GL_TEXTURE_MIN_FILTER, value.gl)
    }

    var magFilter by property(TextureMagFilter.Linear) { value ->
        glTextureParameteri(texture.id, GL_TEXTURE_MAG_FILTER, value.gl)
    }
}