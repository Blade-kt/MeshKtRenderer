package me.blade.meshkt.renderer.objects.shader

import me.blade.meshkt.renderer.objects.ObjectHandle
import me.blade.meshkt.renderer.objects.shader.groups.ShaderStorageBindings
import me.blade.meshkt.renderer.objects.shader.groups.UniformWriter
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20C.*

class Shader : ObjectHandle(glCreateProgram(), false) {
    val storage = ShaderStorageBindings(this)
    val uniforms = UniformWriter(this)

    private val attachedShaders = mutableListOf<Int>()

    fun uniforms(block: UniformWriter.() -> Unit) {
        uniforms.block()
    }

    fun compileSource(
        type: ShaderType,
        block: () -> String
    ) {
        if (type == ShaderType.Compute) {
            check(GL.getCapabilities().GL_ARB_compute_shader) {
                "Compute shader is not supported by the device."
            }
        }

        val shader = glCreateShader(type.gl)

        glShaderSource(shader, block())
        glCompileShader(shader)

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            val log = glGetShaderInfoLog(shader, 2048)
            glDeleteShader(shader)
            throw RuntimeException("Failed to compile $type shader: $log")
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
        storage.free()
    }
}