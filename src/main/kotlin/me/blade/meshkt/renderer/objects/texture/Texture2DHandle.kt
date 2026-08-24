package me.blade.meshkt.renderer.objects.texture

import me.blade.meshkt.renderer.resource.IMeshResource
import org.lwjgl.opengl.GL11C.*

class Texture2DHandle private constructor(
    val id: Int,
    private val deleteOnFree: Boolean
) : IMeshResource {
    override fun free() {
        if (deleteOnFree) {
            glDeleteTextures(id)
        }
    }

    companion object {
        fun create() = Texture2DHandle(glGenTextures(), true)
        fun external(id: Int) = Texture2DHandle(id, false)
    }
}