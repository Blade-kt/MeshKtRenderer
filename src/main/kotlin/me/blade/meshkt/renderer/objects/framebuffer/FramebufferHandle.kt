package me.blade.meshkt.renderer.objects.framebuffer

import me.blade.meshkt.renderer.objects.texture.properties.TextureTarget
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.resource.MeshSyncContext
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL45C.glCreateFramebuffers

class FramebufferHandle private constructor(
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
        ) = FramebufferHandle(glCreateFramebuffers(), true)

        @MeshSyncContext
        fun external(
            id: Int
        ) = FramebufferHandle(id, false)
    }
}