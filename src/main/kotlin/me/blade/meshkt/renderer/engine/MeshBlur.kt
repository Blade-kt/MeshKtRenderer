package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.objects.createShader
import me.blade.meshkt.renderer.objects.createTexture
import me.blade.meshkt.renderer.objects.createViewportFramebuffer
import me.blade.meshkt.renderer.objects.framebuffer.ViewportFramebuffer
import me.blade.meshkt.renderer.objects.framebuffer.properties.FramebufferAttachment
import me.blade.meshkt.renderer.objects.texture.properties.TextureInternalFormat
import me.blade.meshkt.renderer.objects.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.objects.texture.properties.TextureWrap
import me.blade.meshkt.renderer.util.resourceText
import me.blade.meshkt.renderer.util.vec.Vec2
import me.blade.meshkt.renderer.util.vec.Vec2i
import me.blade.meshkt.renderer.util.vec.Vec4
import me.blade.meshkt.renderer.util.vec.Vec4i
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW.glfwGetTime
import org.lwjgl.opengl.ARBDirectStateAccess.glNamedFramebufferReadBuffer
import org.lwjgl.opengl.GL11.GL_RGBA
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL45C.glReadnPixels
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

object MeshBlur : IRenderer {
    private val fbo0 = createViewportFramebuffer {
        attachments[FramebufferAttachment.Color0] = createTexture {
            storage.internalFormat = TextureInternalFormat.RGBA8
            wrapping.wrapS = TextureWrap.ClampToEdge
            wrapping.wrapT = TextureWrap.ClampToEdge
        }

        drawTargets = arrayOf(FramebufferAttachment.Color0)
    }

    private val fbo1 = createViewportFramebuffer {
        attachments[FramebufferAttachment.Color0] = createTexture {
            storage.internalFormat = TextureInternalFormat.RGBA8
            wrapping.wrapS = TextureWrap.ClampToEdge
            wrapping.wrapT = TextureWrap.ClampToEdge
        }

        drawTargets = arrayOf(FramebufferAttachment.Color0)
    }

    private val blurShader = createShader {
        vertex(resourceText("/me/blade/mesh/shaders/blur.vsh"))
        fragment(resourceText("/me/blade/mesh/shaders/blur.fsh"))
        link()
    }

    var projectionMatrix = Matrix4f()

    fun blur(
        pos1: Vec2, pos2: Vec2,
        roundRadiusRightBottom: Double,
        roundRadiusRightTop: Double,
        roundRadiusLeftBottom: Double,
        roundRadiusLeftTop: Double,
        strength: Int, downscale: Int = 2
    ) {
        check(downscale >= 1) {
            "downscale must be >= 1"
        }
        if (strength <= 0) return

        Mesh.signal(this)

        val prevFramebuffer = Mesh.writeFramebuffer
        val prevDepthTest = Mesh.depthTest
        val prevBlend = Mesh.blend
        val prevViewport = Mesh.viewport

        Mesh.boundShader = blurShader
        Mesh.depthTest = false
        Mesh.blend = false

        blurShader.uniforms {
            mat4("u_PROJECTION_MATRIX", projectionMatrix)

            val expand = strength * 5 * downscale
            vec2("u_POS1", pos1.x - expand, pos1.y - expand)
            vec2("u_POS2", pos2.x + expand, pos2.y + expand)
        }

        val viewportSize = Vec2i.create(Mesh.viewport.z, Mesh.viewport.w)
        val blurViewportSizeD = Vec2.create(viewportSize.x / downscale.toDouble(), viewportSize.y / downscale.toDouble())
        val blurViewportSize = Vec2i.create(blurViewportSizeD.x.roundToInt(), blurViewportSizeD.y.roundToInt())
        Mesh.viewport = Vec4i.create(0, 0, blurViewportSize.x, blurViewportSize.y)
        fbo0.update(blurViewportSize)
        fbo0.clearAttachments(FramebufferAttachment.Color0)

        fbo1.update(blurViewportSize)
        fbo1.clearAttachments(FramebufferAttachment.Color0)

        fbo0.blitFrom(
            prevFramebuffer,
            0, 0,
            viewportSize.x, viewportSize.y,
            0, 0,
            blurViewportSize.x, blurViewportSize.y,
            filter = TextureMagFilter.Linear
        )

        blurShader.uniforms.vec2(
            "u_TEXEL_SIZE",
            1.0 / blurViewportSize.x,
            1.0 / blurViewportSize.y
        )

        val fbo0Color = fbo0.attachments[FramebufferAttachment.Color0]!!
        val fbo1Color = fbo1.attachments[FramebufferAttachment.Color0]!!

        fun horizontal() {
            blurShader.uniforms {
                int("u_IS_FLUSH_DRAW", 0)
                bindlessSampler("u_INPUT_TEXTURE", fbo0Color)
                vec2("u_DIRECTION", 1.0, 0.0)
            }

            Mesh.writeFramebuffer = fbo1
            Mesh.render(blurShader, 1)
        }

        fun vertical(isFinal: Boolean = false) {

            blurShader.uniforms {
                bindlessSampler("u_INPUT_TEXTURE", fbo1Color)
                vec2("u_DIRECTION", 0.0, 1.0)

                int("u_IS_FLUSH_DRAW", if (isFinal) 1 else 0)

                if (isFinal) {
                    vec2("u_POS1", pos1.x, pos1.y)
                    vec2("u_POS2", pos2.x, pos2.y)
                }

                val maxRound = min((pos2.x - pos1.x), (pos2.y - pos1.y)) * 0.5
                vec4("u_ROUND_RADIUS",
                    roundRadiusRightBottom.coerceAtMost(maxRound),
                    roundRadiusRightTop.coerceAtMost(maxRound),
                    roundRadiusLeftBottom.coerceAtMost(maxRound),
                    roundRadiusLeftTop.coerceAtMost(maxRound)
                )
            }

            if (isFinal) Mesh.viewport = prevViewport
            Mesh.writeFramebuffer = if (isFinal) prevFramebuffer else fbo1
            Mesh.render(blurShader, 1)
        }

        horizontal()
        repeat((strength - 1).coerceAtLeast(0)) {
            vertical()
            horizontal()
        }
        // technically blending MUST be enabled here
        // but user should be responsible for that, not the engine, so yeah

        // TODO: separate pass to blit to main framebuffer
        vertical(true)
        Mesh.blend = prevBlend
        Mesh.depthTest = prevDepthTest
    }

    override fun flush() {}
    override fun reset() {}
}