package me.blade.meshkt.renderer.texture.properties

import org.lwjgl.opengl.GL12C.*

enum class TexturePixelType(val gl: Int, val bytesPerPixel: Int) {
    UnsignedByte(GL_UNSIGNED_BYTE, 1),
    Byte(GL_BYTE, 1),

    UnsignedShort(GL_UNSIGNED_SHORT, 2),
    Short(GL_SHORT, 2),

    UnsignedInt(GL_UNSIGNED_INT, 4),
    Int(GL_INT, 4),

    Float(GL_FLOAT, 4),
}