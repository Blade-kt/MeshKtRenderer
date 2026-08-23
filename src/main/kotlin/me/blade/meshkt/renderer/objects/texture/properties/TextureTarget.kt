package me.blade.meshkt.renderer.objects.texture.properties

import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL12C.GL_TEXTURE_3D

enum class TextureTarget(val gl: Int) {
    Texture1D(GL_TEXTURE_2D),
    Texture2D(GL_TEXTURE_2D),
    Texture3D(GL_TEXTURE_3D)
}