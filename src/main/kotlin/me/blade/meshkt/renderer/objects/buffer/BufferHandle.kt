package me.blade.meshkt.renderer.objects.buffer

import me.blade.meshkt.renderer.objects.ObjectHandle
import org.lwjgl.opengl.GL15C.glDeleteBuffers
import org.lwjgl.opengl.GL45C.glCreateBuffers

class BufferHandle(
    identifier: Int = glCreateBuffers()
) : ObjectHandle(identifier, false) {
    override fun delete() {
        glDeleteBuffers(id)
    }
}