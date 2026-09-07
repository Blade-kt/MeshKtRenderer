package me.blade.meshkt.renderer.objects.framebuffer

import me.blade.meshkt.renderer.objects.createTexture
import me.blade.meshkt.renderer.util.vec.Vec2i

class ViewportFramebuffer : Framebuffer(null) {
    private var lastViewportSize: Vec2i? = null
    val size: Vec2i get() = lastViewportSize ?: throw IllegalStateException("ViewportFramebuffer is not resized yed")

    fun update(viewportSize: Vec2i) {
        if (viewportSize.x <= 0 || viewportSize.y <= 0) return
        if (lastViewportSize == viewportSize) return

        attachments.entries.forEach { (attachment, texture) ->
            val oldTexture = texture ?: return@forEach

            val newTexture = createTexture {
                migrateFrom(oldTexture)

                storage {
                    internalFormat = oldTexture.storage.internalFormat
                    width = viewportSize.x
                    height = viewportSize.y

                    oldTexture.free()
                    allocate()
                }
            }

            attachments[attachment] = newTexture
        }

        validate()
        lastViewportSize = viewportSize
    }
}