package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.objects.shader.createShader
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import me.blade.meshkt.renderer.pipeline.renderPass
import me.blade.meshkt.renderer.threading.PullingStrategy
import org.joml.Math.clamp
import java.util.logging.Logger

object MeshRenderer {
    private val logger: Logger = Logger.getLogger("MeshKt")

    val executor = createExecutor(PullingStrategy.Allocative, logger)
    val context = createContext(executor)

    val shader = createShader {
        fun loadShaderText(path: String): String {
            val stream = javaClass.getResourceAsStream(path)!!
            return stream.bufferedReader(Charsets.UTF_8).readText()
        }

        compileSource(ShaderType.Vertex) {
            loadShaderText("/mesh/shaders/renderer.vsh")
        }

        compileSource(ShaderType.Fragment) {
            loadShaderText("/mesh/shaders/renderer.fsh")
        }
    }

    fun frame() {
        renderPass(context) {

        }
    }

    private fun packColor(r: Float, g: Float, b: Float, a: Float = 1.0f): Int {
        val rByte = clamp(((r * 255).toInt()), 0, 255)
        val gByte = clamp(((g * 255).toInt()), 0, 255)
        val bByte = clamp(((b * 255).toInt()), 0, 255)
        val aByte = clamp(((a * 255).toInt()), 0, 255)

        return (aByte shl 24) or (rByte shl 16) or (gByte shl 8) or bByte
    }

    private fun packMatrices(
        projectionMatrix: Int,
        viewMatrix: Int,
        modelMatrix: Int,
    ): Int {
        require(projectionMatrix in 0..0xF) {
            "Projection matrix must be 4-bit (0..15), got $projectionMatrix"
        }
        require(viewMatrix in 0..0xFFFFF) {
            "View matrix must be 20-bit (0..1_048_575), got $viewMatrix"
        }
        require(modelMatrix in 0..0xFF) {
            "Model matrix must be 8-bit (0..255), got $modelMatrix"
        }
        return (projectionMatrix shl 28) or (viewMatrix shl 8) or modelMatrix
    }

    fun packInstance(bufferIndex: Int, instanceIndex: Int): Int {
        require(bufferIndex in 0..0xF) {
            "Buffer index must be 4 bits (0-15), got $bufferIndex"
        }
        require(instanceIndex in 0..0x0FFFFFFF) {
            "Instance index must be 28 bits (0-268435455), got $instanceIndex"
        }
        return (bufferIndex shl 28) or instanceIndex
    }
}