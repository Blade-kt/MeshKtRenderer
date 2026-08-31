package me.blade.meshkt.renderer.objects.buffer

import me.blade.meshkt.renderer.objects.ObjectHandle
import org.joml.Matrix4f
import org.lwjgl.opengl.GL15C.GL_DYNAMIC_DRAW
import org.lwjgl.opengl.GL15C.glDeleteBuffers
import org.lwjgl.opengl.GL45C.glCreateBuffers
import org.lwjgl.opengl.GL45C.glNamedBufferData
import org.lwjgl.opengl.GL45C.nglNamedBufferSubData
import org.lwjgl.system.MemoryUtil.*
import kotlin.math.max

class Buffer(initialCapacity: Long) : ObjectHandle(glCreateBuffers(), false) {
    var pointer = NULL
    var capacity = 0L

    var offset = 0L
    private val cursor get() = pointer + offset

    private var dirty = true
    private var capacityDirty = false

    init {
        allocate(initialCapacity)

        // for some drivers to glBindBufferBase an SSBO,
        // you need to allocate it on gpu side (even if it's not gonna get read by the shader)
        // otherwise you will get a GL_INVALID_OPERATION and no rendering output
        glNamedBufferData(id, initialCapacity, GL_DYNAMIC_DRAW)
        capacityDirty = false
    }

    fun reset() {
        offset = 0L
        dirty = true
    }

    fun upload() {
        if (offset == 0L) return
        if (!dirty) return

        if (capacityDirty) {
            capacityDirty = false
            glNamedBufferData(id, capacity, GL_DYNAMIC_DRAW)
        }
        nglNamedBufferSubData(id, 0L, offset, pointer)
        dirty = false
    }

    fun skip(bytes: Int) = write(bytes.toLong()) {}

    fun byte(value: Byte) = write(1) {
        memPutByte(cursor, value)
    }

    fun long(value: Long) = write(8) {
        memPutAddress(cursor, value)
    }

    fun int(value: Int) = write(4) {
        memPutInt(cursor, value)
    }

    fun float(value: Float) = write(4) {
        memPutFloat(cursor, value)
    }

    fun float(value: Double) = write(4) {
        memPutFloat(cursor, value.toFloat())
    }

    fun ivec2(x: Int, y: Int) = write(8) {
        memPutInt(cursor, x)
        memPutInt(cursor + 4, y)
    }

    fun vec2(x: Float, y: Float) = write(8) {
        memPutFloat(cursor, x)
        memPutFloat(cursor + 4, y)
    }

    fun vec2(x: Double, y: Double) = write(8) {
        memPutFloat(cursor, x.toFloat())
        memPutFloat(cursor + 4, y.toFloat())
    }

    fun ivec3(x: Int, y: Int, z: Int) = write(12) {
        memPutInt(cursor, x)
        memPutInt(cursor + 4, y)
        memPutInt(cursor + 8, z)
    }

    fun vec3(x: Float, y: Float, z: Float) = write(12) {
        memPutFloat(cursor, x)
        memPutFloat(cursor + 4, y)
        memPutFloat(cursor + 8, z)
    }

    fun vec3(x: Double, y: Double, z: Double) = write(12) {
        memPutFloat(cursor, x.toFloat())
        memPutFloat(cursor + 4, y.toFloat())
        memPutFloat(cursor + 8, z.toFloat())
    }

    fun ivec4(x: Int, y: Int, z: Int, w: Int) = write(16) {
        memPutInt(cursor, x)
        memPutInt(cursor + 4, y)
        memPutInt(cursor + 8, z)
        memPutInt(cursor + 12, w)
    }

    fun vec4(x: Float, y: Float, z: Float, w: Float) = write(16) {
        memPutFloat(cursor, x)
        memPutFloat(cursor + 4, y)
        memPutFloat(cursor + 8, z)
        memPutFloat(cursor + 12, w)
    }

    fun vec4(x: Double, y: Double, z: Double, w: Double) = write(16) {
        memPutFloat(cursor, x.toFloat())
        memPutFloat(cursor + 4, y.toFloat())
        memPutFloat(cursor + 8, z.toFloat())
        memPutFloat(cursor + 12, w.toFloat())
    }

    fun mat4(value: Matrix4f) = write(64) {
        memPutFloat(cursor     , value.m00())
        memPutFloat(cursor +  4, value.m01())
        memPutFloat(cursor +  8, value.m02())
        memPutFloat(cursor + 12, value.m03())
        memPutFloat(cursor + 16, value.m10())
        memPutFloat(cursor + 20, value.m11())
        memPutFloat(cursor + 24, value.m12())
        memPutFloat(cursor + 28, value.m13())
        memPutFloat(cursor + 32, value.m20())
        memPutFloat(cursor + 36, value.m21())
        memPutFloat(cursor + 40, value.m22())
        memPutFloat(cursor + 44, value.m23())
        memPutFloat(cursor + 48, value.m30())
        memPutFloat(cursor + 52, value.m31())
        memPutFloat(cursor + 56, value.m32())
        memPutFloat(cursor + 60, value.m33())
    }

    private inline fun write(bytes: Long, block: () -> Unit) {
        val shouldGrow = (offset + bytes) > capacity
        if (shouldGrow) {
            allocate(capacity + max(capacity, bytes))
        }
        block()
        dirty = true
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
            nmemFree(pointer)
        }

        pointer = newPointer
        capacity = targetCapacity
        capacityDirty = true
    }

    override fun delete() {
        glDeleteBuffers(id)

        if (pointer != NULL) {
            nmemFree(pointer)
        }
    }
}