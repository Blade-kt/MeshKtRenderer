package me.blade.meshkt

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.engine.MatrixType
import me.blade.meshkt.renderer.engine.MeshRenderer
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.system.MemoryStack
import java.awt.Color
import java.nio.IntBuffer
import kotlin.properties.Delegates

object MeshRendererExample {
    var viewportWidth = 1
    var viewportHeight = 1
    var window by Delegates.notNull<Long>()

    @JvmStatic
    fun main(args: Array<String>) {
        mainEntry()
    }

    fun frame() {
        Mesh.setupState()
        MeshRenderer.apply {
            bindMatrix(MatrixType.Projection, Matrix4f().ortho(0f, viewportWidth.toFloat(), viewportHeight.toFloat(), 0f, -1f, 1f))

            val string = "SomeAshitty_string@ё"
            putRect(30.0, 300.0 - 100.0, 30.0 + stringWidth(string, 100.0), 300.0, Color.DARK_GRAY)
            putString(string, 30.0, 300.0, 100.0)
        }
        MeshRenderer.flush()
        MeshRenderer.fence()
        Mesh.revertState()
    }

    private fun mainEntry() {
        glfwInit()

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_FALSE)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        window = glfwCreateWindow(1024, 768, "MeshRenderer Demo", 0L, 0L)

        glfwMakeContextCurrent(window)
        val caps = GL.createCapabilities()
        println("Bindless texture support: ${caps.GL_ARB_bindless_texture}")

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