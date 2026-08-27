package me.blade.meshkt.renderer.objects.shader

import me.blade.meshkt.renderer.objects.ObjectHandle
import me.blade.meshkt.renderer.objects.buffer.Buffer
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.util.ObservableMap.Companion.observableMap
import org.lwjgl.opengl.GL20C.*
import org.lwjgl.opengl.GL30C.glBindBufferBase
import org.lwjgl.opengl.GL43C.*

class Shader : IMeshResource {
    val handle = object : ObjectHandle(glCreateProgram(),false) {
        override fun delete() {
            glDeleteProgram(id)
        }
    }

    private var ssboIndex = 0
    val storageBindings = observableMap<String, Buffer> { name, buffer ->
        val blockIndex = glGetProgramResourceIndex(handle.id, GL_SHADER_STORAGE_BLOCK, name)

        ssboIndex = (ssboIndex + 1 % 16)
        glShaderStorageBlockBinding(handle.id, blockIndex, ssboIndex)
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, ssboIndex, buffer?.handle?.id ?: 0)
    }

    private val attachedShaders = mutableListOf<Int>()

    fun use(block: Shader.() -> Unit) {
        ssboIndex = 0
        glUseProgram(handle.id)
        block()
        glUseProgram(0)
    }

    fun compileSource(
        type: ShaderType,
        block: () -> String
    ) {
        val shader = glCreateShader(type.gl)

        glShaderSource(shader, block())
        glCompileShader(shader)

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            val log = glGetShaderInfoLog(handle.id, 1024)
            //glDeleteShader(shader)
            //println("Failed to compile $type shader: $log")
        }

        glAttachShader(handle.id, shader)
        attachedShaders.add(shader)
    }

    fun link() {
        glLinkProgram(handle.id)

        if (glGetProgrami(handle.id, GL_LINK_STATUS) == GL_FALSE) {
            val log = glGetProgramInfoLog(handle.id, 1024)
            glDeleteProgram(handle.id)
            throw RuntimeException("Shader linking failed: $log")
        }

        attachedShaders.forEach {
            glDeleteShader(it)
        }
    }

    override fun free() {
        handle.free()
    }
}