package me.blade.meshkt.renderer.objects.buffer

import me.blade.meshkt.renderer.objects.buffer.properties.BufferAccess
import me.blade.meshkt.renderer.resource.IMeshResource
import org.lwjgl.opengl.GL45C.*

class Buffer : IMeshResource {
    val handle = BufferHandle(glCreateBuffers())

    private var allocated = false
    private var sync: Long? = null

    fun allocate(size: Long, flags: Int) {
        check(!allocated) {
            "Buffer storage is already allocated"
        }
        check(size > 0) {
            "Buffer size must be greater than zero"
        }
        glNamedBufferStorage(handle.id, size, flags)
        allocated = true
    }

    fun map(access: BufferAccess) =
        nglMapNamedBuffer(handle.id, access.gl)

    fun unmap() =
        glUnmapNamedBuffer(handle.id)

    fun mapped(access: BufferAccess = BufferAccess.WriteOnly, block: (Long) -> Unit) {
        val pointer = map(access)
        block(pointer)
        glUnmapNamedBuffer(handle.id)
    }

    fun awaitFence() = sync?.let {
        while(true) {
            val waitReturn = glClientWaitSync(it, GL_SYNC_FLUSH_COMMANDS_BIT, 1)
            if (waitReturn == GL_ALREADY_SIGNALED || waitReturn == GL_CONDITION_SATISFIED) break
        }
    }

    fun placeFence() {
        sync?.let(::glDeleteSync)
        sync = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0)
    }

    override fun free() {
        handle.free()
    }
}