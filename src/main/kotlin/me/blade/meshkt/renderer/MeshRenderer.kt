package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.objects.buffer.createBuffer
import me.blade.meshkt.renderer.objects.shader.createShader
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import me.blade.meshkt.renderer.pipeline.renderPass
import me.blade.meshkt.renderer.threading.PullingStrategy
import me.blade.meshkt.renderer.util.packVec2
import org.joml.Math.clamp
import org.joml.Matrix4f
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

    val buffer = createBuffer()

    fun frame() {
        buffer.reset()

        buffer.int(packVec2(
            4, 28,
            0, 0
        ))
        buffer.mat(Matrix4f())

        renderPass(context) {

        }
    }
}