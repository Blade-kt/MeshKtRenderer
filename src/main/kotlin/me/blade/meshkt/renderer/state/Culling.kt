package me.blade.meshkt.renderer.state

import org.lwjgl.opengl.GL11C.GL_BACK
import org.lwjgl.opengl.GL11C.GL_CCW
import org.lwjgl.opengl.GL11C.GL_CW
import org.lwjgl.opengl.GL11C.GL_FRONT
import org.lwjgl.opengl.GL11C.GL_FRONT_AND_BACK

enum class CullFaceMode(val gl: Int) {
    Front(GL_FRONT),
    Back(GL_BACK),
    FrontAndBack(GL_FRONT_AND_BACK)
}

enum class FrontFace(val gl: Int) {
    Clockwise(GL_CW),
    CounterClockwise(GL_CCW)
}