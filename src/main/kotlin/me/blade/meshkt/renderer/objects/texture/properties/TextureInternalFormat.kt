package me.blade.meshkt.renderer.objects.texture.properties

import org.lwjgl.opengl.GL30C.*

enum class TextureInternalFormat(val gl: Int) {
    // Single-component
    R8(GL_R8),
    R16F(GL_R16F),
    R32F(GL_R32F),

    // Two-component
    RG8(GL_RG8),
    RG16F(GL_RG16F),
    RG32F(GL_RG32F),

    // RGB formats
    RGB8(GL_RGB8),
    RGB16F(GL_RGB16F),
    RGB32F(GL_RGB32F),
    SRGB8(GL_SRGB8),

    // RGBA formats
    RGBA8(GL_RGBA8),
    RGBA16F(GL_RGBA16F),
    RGBA32F(GL_RGBA32F),
    SRGB8Alpha8(GL_SRGB8_ALPHA8),
    RGB10A2(GL_RGB10_A2),

    // Depth formats
    Depth16(GL_DEPTH_COMPONENT16),
    Depth24(GL_DEPTH_COMPONENT24),
    Depth32F(GL_DEPTH_COMPONENT32F),

    // Depth-stencil formats
    Depth24Stencil8(GL_DEPTH24_STENCIL8),
    Depth32FStencil8(GL_DEPTH32F_STENCIL8);
}