package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.objects.framebuffer.createFramebuffer
import me.blade.meshkt.renderer.objects.shader.createShader
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import me.blade.meshkt.renderer.objects.ssbo.createShaderStorageBuffer
import me.blade.meshkt.renderer.pipeline.renderPass
import me.blade.meshkt.renderer.threading.PullingStrategy
import java.util.logging.Logger

object MeshRenderer {
    private val logger: Logger = Logger.getLogger("MeshKt")

    val executor = createExecutor(PullingStrategy.Allocative, logger)
    val context = createContext(executor)

    val framebuffer = createFramebuffer {

    }

    val shader = createShader {
        compileSource(ShaderType.Vertex) {
            ""
        }

        compileSource(ShaderType.Fragment) {
            ""
        }
    }

    val ssbo1 = createShaderStorageBuffer(1024) {

    }

    fun frame() {
        renderPass(context) {

        }
    }
}