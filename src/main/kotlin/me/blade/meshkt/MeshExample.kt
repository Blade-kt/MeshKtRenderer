package me.blade.meshkt

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.engine.MatrixType
import me.blade.meshkt.renderer.engine.descriptors.ScissorData
import me.blade.meshkt.renderer.engine.font.buildGlyphMap
import me.blade.meshkt.renderer.objects.createTexture
import me.blade.meshkt.renderer.state.BlendFunc
import me.blade.meshkt.renderer.state.ColorMask
import me.blade.meshkt.renderer.util.vec.Vec2
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.*
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

    private var lastPrint = 0L
    private val time get() = System.currentTimeMillis()
    private var frames = 0
    fun frame() {
        if (time - lastPrint > 1000L) {
            lastPrint = time
            println(frames)
            frames = 0
        }
        frames++

        val projectionMatrix = Matrix4f().ortho(
            0f, viewportWidth.toFloat(),
            viewportHeight.toFloat(), 0f,
            -1f, 1f
        )

        Mesh.frameBegin()
        Mesh.setupState()

        Mesh.blend = true
        Mesh.blendFunc = BlendFunc.default

        Mesh.dispatcherUI.use {
            bindMatrix(MatrixType.Projection, projectionMatrix)

            val textDescriptor = createTextDescriptor {
                content = "BladeCore"
                pos = Vec2.create(50.0, 150.0)
                height = 50.0
                color = Color.RED
            }

            pushScissor(ScissorData(
                Vec2.create(textDescriptor.pos.x, textDescriptor.pos.y - textDescriptor.height),
                Vec2.create(textDescriptor.pos.x + fontWidth(textDescriptor), textDescriptor.pos.y - textDescriptor.height * 0.5),
            ))

            repeat(1) {
                text(textDescriptor)
            }
        }

        Mesh.dispatcherUI.flush()
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