package me.blade.meshkt.renderer.renderpass

import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.threading.RenderThreadExecutor
import me.blade.meshkt.renderer.util.ObservableEnumMap.Companion.observableEnumMap
import org.lwjgl.opengl.GL45C.*

class RenderContextBridge(
    private val executor: RenderThreadExecutor
) {
    /* Textures */
    private var prevTextureSlot = TextureSlot.Slot0.textureSlot
    private val prevBoundTextures = observableEnumMap<TextureSlot, Int>()

    /* Framebuffers */
    private var prevReadFramebuffer = 0
    private var prevWriteFramebuffer = 0

    private val context = RenderContext()
    private var contextReady = false

    fun use(block: RenderContext.() -> Unit) {
        check(contextReady && executor.isOnRenderThread()) {
            "RenderContext cannot be used now. " +
                    "You can only use it between both RenderContext.begin() and RenderContext.end() method calls " +
                    "and only on the main thread. (Ensure RenderThreadExecutor.pollEvents() is called at the start of the frame)"
        }

        reset()
        block(context)
    }

    private fun reset() {
        prevReadFramebuffer = glGetInteger(GL_READ_FRAMEBUFFER)
        prevWriteFramebuffer = glGetInteger(GL_DRAW_FRAMEBUFFER)
        context.readFramebuffer = null
        context.writeFramebuffer = null
    }

    fun begin() {
        prevTextureSlot = glGetInteger(GL_ACTIVE_TEXTURE)

        TextureSlot.entries.forEach { slot ->
            glActiveTexture(slot.textureSlot)
            prevBoundTextures[slot] = glGetInteger(GL_TEXTURE_BINDING_2D)
            context.boundTexture[slot] = null
        }

        glActiveTexture(TextureSlot.Slot0.textureSlot)
        contextReady = true
    }

    fun end() {
        contextReady = false
        prevBoundTextures.entries.forEach { (slot, id) ->
            glActiveTexture(slot.textureSlot)
            glBindTexture(GL_TEXTURE_2D, id ?: 0)
        }

        glActiveTexture(prevTextureSlot)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, prevReadFramebuffer)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, prevWriteFramebuffer)
    }
}