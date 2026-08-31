package me.blade.meshkt.renderer.objects.shader.groups

import me.blade.meshkt.renderer.objects.shader.Shader
import org.joml.Matrix4f
import org.lwjgl.opengl.GL20C.*
import java.nio.FloatBuffer

class UniformWriter(private val shader: Shader) {
    private val uniformCache = mutableMapOf<String, Int>()

    fun int(name: String, value: Int) {
        glUniform1i(getUniformLocation(name), value)
    }

    fun float(name: String, value: Float) {
        glUniform1f(getUniformLocation(name), value)
    }

    fun float(name: String, value: Double) {
        glUniform1f(getUniformLocation(name), value.toFloat())
    }

    fun ivec2(name: String, x: Int, y: Int) {
        glUniform2i(getUniformLocation(name), x, y)
    }

    fun vec2(name: String, x: Float, y: Float) {
        glUniform2f(getUniformLocation(name), x, y)
    }

    fun vec2(name: String, x: Double, y: Double) {
        glUniform2f(getUniformLocation(name), x.toFloat(), y.toFloat())
    }

    fun ivec3(name: String, x: Int, y: Int, z: Int) {
        glUniform3i(getUniformLocation(name), x, y, z)
    }

    fun vec3(name: String, x: Float, y: Float, z: Float) {
        glUniform3f(getUniformLocation(name), x, y, z)
    }

    fun vec3(name: String, x: Double, y: Double, z: Double) {
        glUniform3f(getUniformLocation(name), x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun ivec4(name: String, x: Int, y: Int, z: Int, w: Int) {
        glUniform4i(getUniformLocation(name), x, y, z, w)
    }

    fun vec4(name: String, x: Float, y: Float, z: Float, w: Float) {
        glUniform4f(getUniformLocation(name), x, y, z, w)
    }

    fun vec4(name: String, x: Double, y: Double, z: Double, w: Double) {
        glUniform4f(getUniformLocation(name), x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())
    }

    private val matrix4fBuffer = FloatArray(16)
    fun mat4(name: String, matrix: Matrix4f) {
        matrix.get(matrix4fBuffer)
        glUniformMatrix4fv(getUniformLocation(name), false, matrix4fBuffer)
    }

    private fun getUniformLocation(name: String): Int {
        return uniformCache.getOrPut(name) {
            glGetUniformLocation(shader.id, name)
        }
    }
}