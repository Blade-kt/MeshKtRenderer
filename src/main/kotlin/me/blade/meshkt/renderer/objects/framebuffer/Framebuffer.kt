package me.blade.meshkt.renderer.objects.framebuffer

import me.blade.meshkt.renderer.engine.MeshEngine
import me.blade.meshkt.renderer.objects.framebuffer.properties.FramebufferAttachment
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.util.ObservableEnumMap.Companion.observableEnumMap
import org.lwjgl.opengl.GL45C.glNamedFramebufferTexture


class Framebuffer(val handle: FramebufferHandle) : IMeshResource {
    val attachments = observableEnumMap<FramebufferAttachment, Texture?> { attachment, texture ->
        glNamedFramebufferTexture(handle.id, attachment.gl, texture?.handle?.id ?: 0, 0)
    }

    override fun free() {
        handle.free()
    }

    companion object {
        fun MeshEngine.createFramebuffer(
            handle: FramebufferHandle,
            block: Framebuffer.() -> Unit = {}
        ): Framebuffer {
            val texture = Framebuffer(handle)
            resources.registerResource(texture)

            block(texture)
            return texture
        }
    }
}