package me.blade.meshkt.renderer.objects.texture

import me.blade.meshkt.renderer.objects.ObjectHandle
import org.lwjgl.opengl.GL11C.glDeleteTextures

class TextureHandle(
    identifier: Int,
    isExternal: Boolean
) : ObjectHandle(identifier, isExternal) {
    override fun delete() {
        glDeleteTextures(id)
    }
}