package me.blade.meshkt.renderer.texture

import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.texture.properties.TextureTarget
import org.lwjgl.opengl.GL45C.*

class TextureHandle(target: TextureTarget): IMeshResource {
    val gl = glCreateTextures(target.gl)

    override fun free() {
        glDeleteTextures(gl)
    }
}