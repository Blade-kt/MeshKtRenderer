package me.blade.meshkt.renderer.objects.buffer

import me.blade.meshkt.renderer.objects.ObjectHandle
import org.lwjgl.opengl.GL15C.glDeleteBuffers

class BufferHandle(
    identifier: Int
) : ObjectHandle(identifier, false) {
    override fun delete() {
        glDeleteBuffers(id)
    }
}