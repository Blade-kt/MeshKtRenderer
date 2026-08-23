package me.blade.meshkt

import me.blade.meshkt.renderer.MeshRenderer
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.system.MemoryStack
import java.nio.IntBuffer

object MeshRendererExample {
    @JvmStatic
    fun main(args: Array<String>) {
        glfwInit()

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        val window = glfwCreateWindow(1024, 768, "MeshRenderer Demo", 0L, 0L)

        glfwMakeContextCurrent(window)
        GL.createCapabilities()

        val stack = MemoryStack.stackPush()
        val width: IntBuffer = stack.ints(0)
        val height: IntBuffer = stack.ints(0)
        val fbWidth = width.get(0)
        val fbHeight = height.get(0)
        glfwGetFramebufferSize(window, width, height)
        stack.pop()

        glViewport(0, 0,fbWidth, fbHeight)
        glfwSetFramebufferSizeCallback(window) { _, width, height ->
            glViewport(0, 0, width, height)
        }

        while (true) {
            if (glfwWindowShouldClose(window)) {
                break
            }

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            MeshRenderer.engine.dispatcher.execute()

            glfwSwapBuffers(window)
            glfwPollEvents()
        }

        MeshRenderer.engine.dispatcher.free()
        glfwDestroyWindow(window)
        glfwTerminate()
    }
}