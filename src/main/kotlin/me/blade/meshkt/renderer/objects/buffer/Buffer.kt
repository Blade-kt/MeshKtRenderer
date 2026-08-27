package me.blade.meshkt.renderer.objects.buffer

import me.blade.meshkt.renderer.resource.IMeshResource
import org.lwjgl.opengl.GL15C.GL_DYNAMIC_DRAW
import org.lwjgl.opengl.GL45C.glNamedBufferData
import org.lwjgl.opengl.GL45C.nglNamedBufferSubData
import org.lwjgl.system.MemoryUtil.*

class Buffer(initialCapacity: Long) : IMeshResource {
    val handle = BufferHandle()

    private var pointer = NULL
    private var capacity = 0L

    private var offset = 0L
    private val cursor get() = pointer + offset

    init {
        allocate(initialCapacity)
        glNamedBufferData(handle.id, initialCapacity, GL_DYNAMIC_DRAW)
    }

    fun clear() {
        offset = 0L
    }

    fun upload() {
        nglNamedBufferSubData(handle.id, 0L, capacity, pointer)
    }

    // TODO: convert to plus assign operator
    fun int(value: Int) = write(4) {
        memPutInt(cursor, value)
    }

    fun float(value: Float) = write(4) {
        memPutFloat(cursor, value)
    }

    private inline fun write(bytes: Int, block: () -> Unit) {
        val shouldGrow = (offset + bytes) > capacity
        if (shouldGrow) allocate(capacity * 2)
        block()
        offset += bytes
    }

    private fun allocate(targetCapacity: Long) {
        check(targetCapacity > 0) {
            "Capacity must be greater than zero."
        }

        if (capacity >= targetCapacity) return

        val newPointer = nmemAlloc(targetCapacity)
        check(newPointer != NULL) {
            "Failed to allocate $targetCapacity bytes."
        }

        if (pointer != NULL) {
            memCopy(pointer, newPointer, capacity)
        }

        pointer = newPointer
        capacity = targetCapacity
    }

    override fun free() {
        handle.free()
    }
}