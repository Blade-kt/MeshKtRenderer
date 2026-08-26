package me.blade.meshkt.renderer.objects.buffer.properties

import org.lwjgl.opengl.GL15C.*

enum class BufferAccess(val gl: Int) {
    ReadOnly(GL_READ_ONLY),
    WriteOnly(GL_WRITE_ONLY),
    ReadWrite(GL_READ_WRITE)
}