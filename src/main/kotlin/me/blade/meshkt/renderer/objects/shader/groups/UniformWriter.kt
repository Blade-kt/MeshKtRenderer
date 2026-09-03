package me.blade.meshkt.renderer.objects.shader.groups

import me.blade.meshkt.renderer.objects.shader.Shader
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import org.joml.Matrix4f
import org.lwjgl.opengl.GL20C.*
import org.lwjgl.opengl.GL41C.*

class UniformWriter(private val shader: Shader) {
    private val uniformCache = mutableMapOf<String, Int>()

    fun sampler(name: String, value: TextureSlot) {
        glProgramUniform1i(shader.id, getUniformLocation(name), value.unitIndex)
    }

    fun int(name: String, value: Int) {
        glProgramUniform1i(shader.id, getUniformLocation(name), value)
    }

    fun float(name: String, value: Float) {
        glProgramUniform1f(shader.id, getUniformLocation(name), value)
    }

    fun float(name: String, value: Double) {
        glProgramUniform1f(shader.id, getUniformLocation(name), value.toFloat())
    }

    fun ivec2(name: String, x: Int, y: Int) {
        glProgramUniform2i(shader.id, getUniformLocation(name), x, y)
    }

    fun vec2(name: String, x: Float, y: Float) {
        glProgramUniform2f(shader.id, getUniformLocation(name), x, y)
    }

    fun vec2(name: String, x: Double, y: Double) {
        glProgramUniform2f(shader.id, getUniformLocation(name), x.toFloat(), y.toFloat())
    }

    fun ivec3(name: String, x: Int, y: Int, z: Int) {
        glProgramUniform3i(shader.id, getUniformLocation(name), x, y, z)
    }

    fun vec3(name: String, x: Float, y: Float, z: Float) {
        glProgramUniform3f(shader.id, getUniformLocation(name), x, y, z)
    }

    fun vec3(name: String, x: Double, y: Double, z: Double) {
        glProgramUniform3f(shader.id, getUniformLocation(name), x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun ivec4(name: String, x: Int, y: Int, z: Int, w: Int) {
        glProgramUniform4i(shader.id, getUniformLocation(name), x, y, z, w)
    }

    fun vec4(name: String, x: Float, y: Float, z: Float, w: Float) {
        glProgramUniform4f(shader.id, getUniformLocation(name), x, y, z, w)
    }

    fun vec4(name: String, x: Double, y: Double, z: Double, w: Double) {
        glProgramUniform4f(shader.id, getUniformLocation(name), x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())
    }

    private val matrix4fBuffer = FloatArray(16)
    fun mat4(name: String, matrix: Matrix4f) {
        matrix.get(matrix4fBuffer)
        glProgramUniformMatrix4fv(shader.id, getUniformLocation(name), false, matrix4fBuffer)
    }

    private fun getUniformLocation(name: String): Int {
        return uniformCache.getOrPut(name) {
            glGetUniformLocation(shader.id, name)
        }
    }
}