package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.objects.createShader
import me.blade.meshkt.renderer.util.packColorARGB
import me.blade.meshkt.renderer.util.resourceText
import me.blade.meshkt.renderer.util.vec.Vec2
import org.joml.Matrix4f
import java.awt.Color

class MeshLineDispatcher {
    private val sdfShader = createShader {
        vertex(resourceText("/me/blade/mesh/shaders/line.vsh"))
        fragment(resourceText("/me/blade/mesh/shaders/line.fsh"))
        link()
    }

    private val storage = sdfShader.storage

    private val matrixBuffer = storage.allocate("MatrixBuffer", 16 * 4 * 3, true)
    var projectionMatrix = Matrix4f()
    var viewMatrix = Matrix4f()
    var modelMatrix = Matrix4f()

    private val lineBuffer = storage.allocate("LineBuffer")
    private var instanceCount = 0

    fun line(pos1: Vec2, pos2: Vec2, color: Color, width: Double = 1.0) {
        lineBuffer.vec4(pos1.x, pos1.y, pos2.x, pos2.y)
        lineBuffer.int(packColorARGB(color))
        lineBuffer.float(width.coerceIn(1.0..16.0))
        lineBuffer.skip(8)
        instanceCount++
    }

    fun flush() {
        matrixBuffer.apply {
            reset()
            mat4(projectionMatrix)
            mat4(viewMatrix)
            mat4(modelMatrix)
            upload()
        }

        lineBuffer.upload()

        Mesh.boundShader = sdfShader
        Mesh.render(instanceCount)
        instanceCount = 0
    }

    fun reset() {
        lineBuffer.reset()
    }

    fun use(block: MeshLineDispatcher.() -> Unit) {
        block(this)
    }
}