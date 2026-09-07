package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.objects.createShader
import me.blade.meshkt.renderer.objects.createTexture
import me.blade.meshkt.renderer.objects.createViewportFramebuffer
import me.blade.meshkt.renderer.objects.framebuffer.Framebuffer
import me.blade.meshkt.renderer.objects.framebuffer.properties.FramebufferAttachment
import me.blade.meshkt.renderer.objects.texture.properties.TextureInternalFormat
import me.blade.meshkt.renderer.util.packColorARGB
import me.blade.meshkt.renderer.util.rent
import me.blade.meshkt.renderer.util.resourceText
import me.blade.meshkt.renderer.util.vec.Vec2
import me.blade.meshkt.renderer.util.vec.Vec2i
import org.joml.Matrix4f
import org.lwjgl.opengl.GL40.*
import java.awt.Color

class MeshLineDispatcher {
    private val sdfShader = createShader {
        vertex(resourceText("/me/blade/mesh/shaders/line/linesdf.vsh"))
        fragment(resourceText("/me/blade/mesh/shaders/line/linesdf.fsh"))
        link()
    }

    private val blitShader = createShader {
        vertex(resourceText("/me/blade/mesh/shaders/line/lineblit.vsh"))
        fragment(resourceText("/me/blade/mesh/shaders/line/lineblit.fsh"))
        link()
    }

    private val clearShader = createShader {
        vertex(resourceText("/me/blade/mesh/shaders/line/lineblit.vsh"))
        fragment(resourceText("/me/blade/mesh/shaders/line/lineclear.fsh"))
        link()
    }

    private val sdfFBO = createViewportFramebuffer {
        attachments[FramebufferAttachment.Color0] = createTexture {
            storage.internalFormat = TextureInternalFormat.RGBA8
        }
        attachments[FramebufferAttachment.Color1] = createTexture {
            storage.internalFormat = TextureInternalFormat.R8 // sdf
        }
        attachments[FramebufferAttachment.Depth] = createTexture {
            storage.internalFormat = TextureInternalFormat.Depth24
        }

        drawTargets = arrayOf(FramebufferAttachment.Color0, FramebufferAttachment.Color1)
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

    fun applyDepth(sourceFramebuffer: Framebuffer) {
        sourceFramebuffer.blitTo(
            target = sdfFBO,
            srcWidth = sdfFBO.size.x,
            srcHeight = sdfFBO.size.y,
            mask = GL_DEPTH_BUFFER_BIT,
        )
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

        // unsynchronized clean
        sdfFBO.invalidateAttachments(
            FramebufferAttachment.Color0,
            FramebufferAttachment.Color1
        )

        sdfFBO.clearAttachments(FramebufferAttachment.Depth)

        sdfFBO.update(Vec2i.create(
            Mesh.viewport.z, Mesh.viewport.w
        ))

        rent(Mesh::writeFramebuffer, sdfFBO) {
            rent(Mesh::blend, false) {
                // unsynchronized clean
                Mesh.boundShader = clearShader
                Mesh.render(1)

                Mesh.blend = true
                // TODO: State management for this shit
                glBlendFunci(1, GL_ONE, GL_ONE)
                glBlendEquationi(1, GL_MAX)

                Mesh.boundShader = sdfShader
                Mesh.render(instanceCount)
                instanceCount = 0
            }
        }

        Mesh.boundShader = blitShader
        val colorInputTexture = sdfFBO.attachments[FramebufferAttachment.Color0]!!
        val sdfInputTexture = sdfFBO.attachments[FramebufferAttachment.Color1]!!
        blitShader.uniforms.bindlessSampler("COLOR_INPUT_TEXTURE", colorInputTexture)
        blitShader.uniforms.bindlessSampler("SDF_INPUT_TEXTURE", sdfInputTexture)
        Mesh.render(1)
    }

    fun reset() {
        lineBuffer.reset()
    }

    fun use(block: MeshLineDispatcher.() -> Unit) {
        block(this)
    }
}