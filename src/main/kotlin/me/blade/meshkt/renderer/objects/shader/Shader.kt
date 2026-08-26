package me.blade.meshkt.renderer.objects.shader

import me.blade.meshkt.renderer.objects.ObjectHandle
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import me.blade.meshkt.renderer.objects.ssbo.ShaderStorageBuffer
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.util.ObservableMap.Companion.observableMap
import org.lwjgl.opengl.GL20C.*
import org.lwjgl.opengl.GL30C.glBindBufferBase
import org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BLOCK
import org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER
import org.lwjgl.opengl.GL43C.glGetProgramResourceIndex
import org.lwjgl.opengl.GL43C.glShaderStorageBlockBinding

class Shader : IMeshResource {
    val handle = object : ObjectHandle(glCreateProgram(),false) {
        override fun delete() {
            glDeleteProgram(id)
        }
    }

    private var ssboIndex = 0
    val shaderStorageBindings = observableMap<String, ShaderStorageBuffer> { name, ssbo ->
        val blockIndex = glGetProgramResourceIndex(handle.id, GL_SHADER_STORAGE_BLOCK, name)

        ssboIndex = (ssboIndex + 1 % 16)
        glShaderStorageBlockBinding(handle.id, blockIndex, ssboIndex)
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, ssboIndex, ssbo?.handle ?: 0)
    }

    private val attachedShaders = mutableListOf<Int>()

    fun compileSource(
        type: ShaderType,
        block: () -> String
    ) {
        val shader = glCreateShader(type.gl)
        glShaderSource(shader, block())
        glCompileShader(shader)

        if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
            val log = glGetShaderInfoLog(handle.id)
            glDeleteShader(shader)
            throw RuntimeException("Failed to compile shader:\n$log")
        }

        glAttachShader(handle.id, shader)
        attachedShaders.add(shader)
    }

    fun link() {
        glLinkProgram(handle.id)

        if (glGetProgrami(handle.id, GL_LINK_STATUS) == GL_FALSE) {
            val log = glGetProgramInfoLog(handle.id)
            glDeleteProgram(handle.id)
            throw RuntimeException("Shader linking failed:\n$log")
        }

        attachedShaders.forEach {
            glDeleteShader(it)
        }
    }

    override fun free() {
        handle.free()
    }
}