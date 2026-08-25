package me.blade.meshkt.renderer.objects.buffer

import me.blade.meshkt.renderer.objects.ObjectHandle
import org.lwjgl.opengl.GL15C.glDeleteBuffers

class BufferHandle(
    identifier: Int,
    isExternal: Boolean
) : ObjectHandle(identifier, isExternal) {
    override fun delete() {
        glDeleteBuffers(id)
    }
}