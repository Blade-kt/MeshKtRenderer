package me.blade.meshkt.renderer.objects.buffer.properties

import org.lwjgl.opengl.GL45.*

enum class BufferTarget(val gl: Int) {
    VertexArray(GL_ARRAY_BUFFER),
    ElementArray(GL_ELEMENT_ARRAY_BUFFER),

    ShaderStorage(GL_SHADER_STORAGE_BUFFER),
    Uniform(GL_UNIFORM_BUFFER),

    CopyRead(GL_COPY_READ_BUFFER),
    CopyWrite(GL_COPY_WRITE_BUFFER),
}