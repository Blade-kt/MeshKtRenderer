package me.blade.meshkt.renderer.objects.shader.properties

import me.blade.meshkt.renderer.objects.buffer.Buffer
import me.blade.meshkt.renderer.objects.shader.Shader
import org.lwjgl.opengl.GL30C.glBindBufferBase
import org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BLOCK
import org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER
import org.lwjgl.opengl.GL43C.glGetProgramResourceIndex
import org.lwjgl.opengl.GL43C.glShaderStorageBlockBinding

class ShaderStorageBindings(private val shader: Shader) {
    private val appliedBindings = hashMapOf<Int, Buffer>()
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

    fun applyBindings() {
        bindings.forEach { (id, buffer) ->
            if (appliedBindings[id] == buffer) return@forEach
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, id, buffer.id)
            appliedBindings[id] = buffer
        }
    }
}