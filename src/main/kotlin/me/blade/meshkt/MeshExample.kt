package me.blade.meshkt

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.engine.MatrixType
import me.blade.meshkt.renderer.engine.MeshLines
import me.blade.meshkt.renderer.engine.MeshUI
import me.blade.meshkt.renderer.engine.font.buildGlyphMap
import me.blade.meshkt.renderer.objects.createTexture
import me.blade.meshkt.renderer.objects.createViewportFramebuffer
import me.blade.meshkt.renderer.objects.framebuffer.properties.FramebufferAttachment
import me.blade.meshkt.renderer.objects.texture.properties.TextureInternalFormat
import me.blade.meshkt.renderer.objects.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.state.BlendFunc
import me.blade.meshkt.renderer.util.Quad
import me.blade.meshkt.renderer.util.rent
import me.blade.meshkt.renderer.util.vec.Vec2
import me.blade.meshkt.renderer.util.vec.Vec2i
import me.blade.meshkt.renderer.util.vec.Vec4i
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL43C.GL_MAX_COMPUTE_SHARED_MEMORY_SIZE
import org.lwjgl.system.MemoryStack
import java.awt.Color
import java.awt.Font
import java.nio.IntBuffer
import kotlin.properties.Delegates

object MeshExample {
    var viewportWidth = 1
    var viewportHeight = 1
    var window by Delegates.notNull<Long>()

    @JvmStatic
    fun main(args: Array<String>) {
        mainEntry()
    }

    val fbo by lazy {
        createViewportFramebuffer {
            attachments[FramebufferAttachment.Color0] = createTexture {
                storage.internalFormat = TextureInternalFormat.RGBA8
            }

            drawTargets = arrayOf(FramebufferAttachment.Color0)
        }
    }

    fun frame() {
        val projection = Matrix4f().ortho(
            0f, viewportWidth.toFloat(),
            viewportHeight.toFloat(), 0f,
            -1f, 1f
        )

        Mesh.frameBegin()
        Mesh.setupState()

        val viewport0 = Vec2i.create(viewportWidth, viewportHeight)
        val viewport1 = Vec2i.create(viewportWidth * 2, viewportHeight * 2)
        fbo.update(viewport1)
        fbo.clearAttachments(FramebufferAttachment.Color0)
        Mesh.viewport = Vec4i.create(0, 0, viewport1.x, viewport1.y)

        Mesh.blend = true
        Mesh.blendFunc = BlendFunc.CLASSIC

        MeshUI.reset()
        MeshLines.reset()

        MeshUI.bindMatrix(MatrixType.Projection, projection)
        MeshLines.projectionMatrix = projection

        rent(Mesh::writeFramebuffer, fbo) {
            var y = 0.0
            repeat(20) {
                val size = (it + 1) * 3.0

                MeshUI.text {
                    content = "Height: $size"
                    pos = Vec2.create(10.0, 10.0 + y)
                    height = size
                }

                y += size + 10.0
            }

            Mesh.flushRemaining()
        }

        Mesh.viewport = Vec4i.create(0, 0, viewport0.x, viewport0.y)
        fbo.blitTo(
            null,
            srcWidth = viewport1.x, srcHeight = viewport1.y,
            dstWidth = viewport0.x, dstHeight = viewport0.y,
            filter = TextureMagFilter.Linear
        )

        Mesh.revertState()
    }

    private fun mainEntry() {
        glfwInit()

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_FALSE)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

        /*glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE)
        glfwWindowHint(GLFW_MOUSE_PASSTHROUGH, GLFW_TRUE)
        glfwWindowHint(GLFW_FLOATING, GLFW_TRUE)
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE)*/

        val monitorHandle = glfwGetPrimaryMonitor()
        val mode = glfwGetVideoMode(monitorHandle)!!
        window = glfwCreateWindow(mode.width() / 2, mode.height() / 2, "MeshRenderer Demo", 0L, 0L)

        glfwMakeContextCurrent(window)
        GL.createCapabilities()

        val stack = MemoryStack.stackPush()
        val width: IntBuffer = stack.ints(0)
        val height: IntBuffer = stack.ints(0)
        glfwGetFramebufferSize(window, width, height)
        val fbWidth = width.get(0)
        val fbHeight = height.get(0)

        stack.pop()

        glViewport(0, 0,fbWidth, fbHeight)
        viewportWidth = fbWidth
        viewportHeight = fbHeight
        glfwSetFramebufferSizeCallback(window) { _, width, height ->
            glViewport(0, 0, width, height)
            viewportWidth = width
            viewportHeight = height
        }

        glfwSwapInterval(0)

        while (true) {
            if (glfwWindowShouldClose(window)) {
                break
            }

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            frame()

            glfwSwapBuffers(window)
            glfwPollEvents()
        }

        glfwDestroyWindow(window)
        glfwTerminate()
    }
}