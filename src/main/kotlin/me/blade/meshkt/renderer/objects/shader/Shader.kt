package me.blade.meshkt.renderer.objects.shader

import me.blade.meshkt.renderer.objects.ObjectHandle
import me.blade.meshkt.renderer.objects.buffer.Buffer
import me.blade.meshkt.renderer.objects.createBuffer
import me.blade.meshkt.renderer.objects.shader.properties.ShaderStorageBindings
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import org.lwjgl.opengl.GL20C.*

class Shader : ObjectHandle(glCreateProgram(), false) {
    val storage = ShaderStorageBindings(this)

    fun allocateStorage(name: String, initialCapacity: Long = 1024) {
        storage[name] = createBuffer(initialCapacity)
    }

    fun allocateStorages(initialCapacity: Long, vararg names: String) {
        names.forEach {
            allocateStorage(it, initialCapacity)
        }
    }

    fun write(name: String, block: Buffer.() -> Unit) {
        val buffer = storage[name]
        block(buffer)
    }

    private val attachedShaders = mutableListOf<Int>()

    fun compileSource(
        type: ShaderType,
        block: () -> String
    ) {
        val shader = glCreateShader(type.gl)

        glShaderSource(shader, block())
        glCompileShader(shader)

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            val log = glGetShaderInfoLog(id, 1024)
            //glDeleteShader(shader)
            //println("Failed to compile $type shader: $log")
        }

        glAttachShader(id, shader)
        attachedShaders.add(shader)
    }

    fun link() {
        glLinkProgram(id)

        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            val log = glGetProgramInfoLog(id, 1024)
            glDeleteProgram(id)
            throw RuntimeException("Shader linking failed: $log")
        }

        attachedShaders.removeIf {
            glDeleteShader(it); true
        }
    }

    override fun delete() {
        attachedShaders.removeIf {
            glDeleteShader(it); true
        }

        glDeleteProgram(id)
    }
}