package me.blade.meshkt.renderer.objects.texture.groups

import me.blade.meshkt.renderer.objects.texture.TextureHandle
import me.blade.meshkt.renderer.util.GLProperty.Companion.property
import me.blade.meshkt.renderer.objects.texture.properties.TextureWrap
import me.blade.meshkt.renderer.util.Quad
import me.blade.meshkt.renderer.util.Quad.Companion.toList
import org.lwjgl.opengl.GL45C.*

class TextureWrapping(handle: TextureHandle) {
    var wrapS by property(TextureWrap.Repeat) { value ->
        glTextureParameteri(handle.id, GL_TEXTURE_WRAP_S, value.gl)
    }

    var wrapT by property(TextureWrap.Repeat) { value ->
        glTextureParameteri(handle.id, GL_TEXTURE_WRAP_T, value.gl)
    }

    var wrapR by property(TextureWrap.Repeat) { value ->
        glTextureParameteri(handle.id, GL_TEXTURE_WRAP_R, value.gl)
    }

    var borderColor by property(Quad(0f, 0f, 0f, 0f)) { value ->
        glTextureParameterfv(handle.id, GL_TEXTURE_BORDER_COLOR, value.toList().toFloatArray())
    }
}