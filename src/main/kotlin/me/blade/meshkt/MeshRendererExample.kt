package me.blade.meshkt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.blade.meshkt.renderer.MeshRenderer
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwDestroyWindow
import org.lwjgl.glfw.GLFW.glfwGetFramebufferSize
import org.lwjgl.glfw.GLFW.glfwInit
import org.lwjgl.glfw.GLFW.glfwMakeContextCurrent
import org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback
import org.lwjgl.glfw.GLFW.glfwTerminate
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.system.MemoryStack
import java.nio.IntBuffer

object MeshRendererExample {
    @JvmStatic
    fun main(args: Array<String>) {
        glfwInit()

        val window = GLFW.glfwCreateWindow(1024, 768, "MeshRenderer Demo", 0L, 0L)

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
            if (GLFW.glfwWindowShouldClose(window)) {
                break
            }

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
            MeshRenderer.dispatcher.execute()

            GLFW.glfwSwapBuffers(window)
            GLFW.glfwPollEvents()
        }

        MeshRenderer.dispatcher.close()
        glfwDestroyWindow(window)
        glfwTerminate()
    }
}