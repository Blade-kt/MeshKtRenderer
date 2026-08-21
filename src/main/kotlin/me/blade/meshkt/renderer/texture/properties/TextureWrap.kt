package me.blade.meshkt.renderer.texture.properties

import org.lwjgl.opengl.GL14C.*

enum class TextureWrap(val gl: Int) {
    Repeat(GL_REPEAT),
    ClampToEdge(GL_CLAMP_TO_EDGE),
    ClampToBorder(GL_CLAMP_TO_BORDER),
    Mirrored(GL_MIRRORED_REPEAT);
}
