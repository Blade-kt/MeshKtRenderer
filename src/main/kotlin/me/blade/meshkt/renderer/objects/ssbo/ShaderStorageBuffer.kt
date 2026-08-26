package me.blade.meshkt.renderer.objects.ssbo

import me.blade.meshkt.renderer.objects.buffer.createBuffer
import me.blade.meshkt.renderer.objects.buffer.properties.BufferAccess
import me.blade.meshkt.renderer.resource.IMeshResource
import org.lwjgl.opengl.GL44C.*

class ShaderStorageBuffer(bufferSize: Long) : IMeshResource {
    val pointer: Long
    val size = bufferSize

    val handle get() = buffer.handle.id

    private val buffer = createBuffer {
        allocate(bufferSize, GL_MAP_WRITE_BIT or GL_MAP_PERSISTENT_BIT or GL_MAP_COHERENT_BIT)
        pointer = map(BufferAccess.WriteOnly)
    }

    override fun free() {
        buffer.unmap()
        buffer.free()
    }
}