package me.blade.meshkt.renderer.texture

import org.lwjgl.opengl.GL11C.*

enum class TextureMinFilter(val gl: Int) {
    Nearest(GL_NEAREST),
    Linear(GL_LINEAR),

    NearestMipmapNearest(GL_NEAREST_MIPMAP_NEAREST),
    LinearMipmapNearest(GL_LINEAR_MIPMAP_NEAREST),
    NearestMipmapLinear(GL_NEAREST_MIPMAP_LINEAR),
    LinearMipmapLinear(GL_LINEAR_MIPMAP_LINEAR);
}