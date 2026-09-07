package me.blade.meshkt.renderer.state

import org.lwjgl.opengl.GL11C.GL_BACK
import org.lwjgl.opengl.GL11C.GL_CCW
import org.lwjgl.opengl.GL11C.GL_CULL_FACE_MODE
import org.lwjgl.opengl.GL11C.GL_CW
import org.lwjgl.opengl.GL11C.GL_FRONT
import org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK
import org.lwjgl.opengl.GL11C.GL_FRONT_FACE
import org.lwjgl.opengl.GL11C.glCullFace
import org.lwjgl.opengl.GL11C.glFrontFace
import org.lwjgl.opengl.GL11C.glGetInteger

enum class CullFaceMode(override val gl: Int): GLInt {
    Front(GL_FRONT),
    Back(GL_BACK),
    FrontAndBack(GL_FRONT_AND_BACK);

    fun apply() {
        glCullFace(gl)
    }

    companion object {
        private val valueMap = hashMapOf<Int, CullFaceMode>().apply {
            CullFaceMode.entries.forEach { entry ->
                this[entry.gl] = entry
            }
        }

        fun fromGL() = valueMap[glGetInteger(GL_CULL_FACE_MODE)]!!
    }
}

enum class FrontFace(val gl: Int) {
    Clockwise(GL_CW),
    CounterClockwise(GL_CCW);

    fun apply() {
        glFrontFace(gl)
    }

    companion object {
        private val valueMap = hashMapOf<Int, FrontFace>().apply {
            FrontFace.entries.forEach { entry ->
                this[entry.gl] = entry
            }
        }

        fun fromGL() = valueMap[glGetInteger(GL_FRONT_FACE)]!!
    }
}