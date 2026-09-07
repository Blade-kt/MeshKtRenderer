package me.blade.meshkt.renderer.objects.shader.groups

import me.blade.meshkt.renderer.objects.buffer.Buffer
import me.blade.meshkt.renderer.objects.createBuffer
import me.blade.meshkt.renderer.objects.shader.Shader
import me.blade.meshkt.renderer.util.IMeshResource
import org.lwjgl.opengl.GL43C.*

class ShaderStorageBindings(private val shader: Shader) : IMeshResource {
    private val bindings = hashMapOf<Int, Buffer>()

    private var ssboIndex = 0
    private val ssboNameMap = hashMapOf<String, Int>()

    operator fun get(name: String) = bindings[storageIndex(name)] ?: throw IllegalStateException("SSBO binding named '$name' is not linked.")
    operator fun set(name: String, buffer: Buffer) = bindings.set(storageIndex(name), buffer)

    private fun storageIndex(name: String) = ssboNameMap.getOrPut(name) {
        if (++ssboIndex >= 16) throw RuntimeException("Reached SSBO binding limit $ssboIndex")
        val blockIndex = glGetProgramResourceIndex(shader.id, GL_SHADER_STORAGE_BLOCK, name)
        glShaderStorageBlockBinding(shader.id, blockIndex, ssboIndex)
        ssboIndex
    }

    fun allocate(name: String, initialCapacity: Long = 1024, fixed: Boolean = false, block: Buffer.() -> Unit = {}): Buffer {
        val buffer = createBuffer(initialCapacity, fixed)
        this[name] = buffer
        buffer.block()
        return buffer
    }

    fun allocateNames(initialCapacity: Long, fixed: Boolean, vararg names: String) {
        names.forEach {
            allocate(it, initialCapacity, fixed)
        }
    }

    fun write(name: String, block: Buffer.() -> Unit) {
        val buffer = this[name]
        block(buffer)
    }

    fun applyBindings() {
        bindings.forEach { (id, buffer) ->
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, id, buffer.id)
        }
    }

    override fun free() {
        bindings.values.forEach {
            it.free()
        }
    }
}