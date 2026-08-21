package me.blade.meshkt.renderer.texture.properties

import org.lwjgl.opengl.GL30C.*

enum class TexturePixelFormat(val gl: Int, val componentCount: Int) {
    Red(GL_RED, 1),
    RG(GL_RG, 2),
    RGB(GL_RGB, 3),
    BGR(GL_BGR, 3),
    RGBA(GL_RGBA, 4),
    BGRA(GL_BGRA, 4),
    Depth(GL_DEPTH_COMPONENT, 1),
    DepthStencil(GL_DEPTH_STENCIL, 4),
    Stencil(GL_STENCIL_INDEX, 1);
}