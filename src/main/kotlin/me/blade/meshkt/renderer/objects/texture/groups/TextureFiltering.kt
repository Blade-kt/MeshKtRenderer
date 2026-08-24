package me.blade.meshkt.renderer.objects.texture.groups

import me.blade.meshkt.renderer.util.GLProperty.Companion.property
import me.blade.meshkt.renderer.objects.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.objects.texture.properties.TextureMinFilter
import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER
import org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER
import org.lwjgl.opengl.GL11C.glTexParameteri

class TextureFiltering {
    var minFilter by property(TextureMinFilter.NearestMipmapLinear) { value ->
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, value.gl)
    }

    var magFilter by property(TextureMagFilter.Linear) { value ->
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, value.gl)
    }
}