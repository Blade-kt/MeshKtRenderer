package me.blade.meshkt.renderer.state

import org.lwjgl.opengl.GL11C.GL_ALWAYS
import org.lwjgl.opengl.GL11C.GL_EQUAL
import org.lwjgl.opengl.GL11C.GL_GEQUAL
import org.lwjgl.opengl.GL11C.GL_GREATER
import org.lwjgl.opengl.GL11C.GL_LEQUAL
import org.lwjgl.opengl.GL11C.GL_LESS
import org.lwjgl.opengl.GL11C.GL_NEVER
import org.lwjgl.opengl.GL11C.GL_NOTEQUAL

enum class Func(val gl: Int) {
    Never(GL_NEVER),
    Less(GL_LESS),
    Equal(GL_EQUAL),
    Lequal(GL_LEQUAL),
    Greater(GL_GREATER),
    NotEqual(GL_NOTEQUAL),
    Gequal(GL_GEQUAL),
    Always(GL_ALWAYS)
}