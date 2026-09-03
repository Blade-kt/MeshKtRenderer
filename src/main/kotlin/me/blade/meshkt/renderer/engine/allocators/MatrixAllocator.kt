package me.blade.meshkt.renderer.engine.allocators

import me.blade.meshkt.renderer.objects.buffer.Buffer
import org.joml.Matrix4f

class MatrixAllocator(val buffer: Buffer, val bits: Int) {
    private val cache = hashMapOf<Int, Int>()
    val matrices = arrayListOf<Matrix4f>()
    var bound = 0

    private var dirty = true
    private val maxCapacity = 1 shl bits

    fun bind(matrix: Matrix4f, unsafe: Boolean = false) {
        val copy = if (unsafe) matrix else Matrix4f(matrix)

        bound = cache.getOrPut(copy.hashCode()) {
            matrices.add(copy)
            dirty = true

            val newIndex = matrices.lastIndex
            check(newIndex < maxCapacity) {
                "Matrix buffer limit has exceeded ${newIndex+1}/$maxCapacity ($bits bits)"
            }
            newIndex
        }
    }

    fun reset() {
        cache.clear()
        matrices.clear()
        bound = 0
        bind(IDENTITY_MATRIX, true)
        dirty = true
    }

    fun flush() {
        if (!dirty) return
        dirty = false

        buffer.reset()
        matrices.forEach(buffer::mat4)
        buffer.upload()
    }

    companion object {
        private val IDENTITY_MATRIX = Matrix4f().identity()
    }
}