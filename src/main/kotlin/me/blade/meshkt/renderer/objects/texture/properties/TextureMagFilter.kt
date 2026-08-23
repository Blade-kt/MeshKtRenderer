package me.blade.meshkt.renderer.objects.texture.properties

import org.lwjgl.opengl.GL11C.*

enum class TextureMagFilter(val gl: Int) {
    Nearest(GL_NEAREST),
    Linear(GL_LINEAR);
}