package me.blade.meshkt.renderer.objects.texture

import me.blade.meshkt.renderer.objects.texture.properties.TextureTarget
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.resource.MeshSyncContext
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL45C

class TextureHandle private constructor(
    val target: TextureTarget,
    val id: Int,
    private val deleteOnFree: Boolean
) : IMeshResource {
    override fun free() {
        if (deleteOnFree) {
            GL11C.glDeleteTextures(id)
        }
    }

    companion object {
        @MeshSyncContext
        fun create(
            target: TextureTarget
        ) = TextureHandle(target, GL45C.glCreateTextures(target.gl), true)

        @MeshSyncContext
        fun external(
            target: TextureTarget, id: Int
        ) = TextureHandle(target, id, false)
    }
}