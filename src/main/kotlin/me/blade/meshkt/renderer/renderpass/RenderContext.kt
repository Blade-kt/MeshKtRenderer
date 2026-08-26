package me.blade.meshkt.renderer.renderpass

import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.threading.RenderThreadExecutor
import me.blade.meshkt.renderer.util.ObservableEnumMap.Companion.observableEnumMap
import org.lwjgl.opengl.GL45C.*

class RenderContext(
    private val executor: RenderThreadExecutor
) {
    /* Textures */
    private var prevTextureSlot = TextureSlot.Slot0.textureSlot
    private val prevBoundTextures = observableEnumMap<TextureSlot, Int>()

    /* Framebuffers */
    private var prevReadFramebuffer = 0
    private var prevWriteFramebuffer = 0

    /* VAO */
    private var prevVertexArrayObject = 0

    private val renderState = RenderState(this)
    private var renderStateReady = false
    private var isStateInUse = false

    private val vao by lazy {
        glCreateVertexArrays()
    }

    fun acquireRenderState(): RenderState {
        check(isStateInUse) {
            "Cannot acquire render state. It's already in use!"
        }

        check(renderStateReady && executor.isOnRenderThread()) {
            "RenderContext cannot be used now. " +
                    "You can only use it between both RenderContext.begin() and RenderContext.end() method calls " +
                    "and only on the main thread. (Ensure RenderThreadExecutor.pollEvents() is called at the start of the frame)"
        }

        resetState()
        isStateInUse = true
        return renderState
    }

    private fun resetState() {
        TextureSlot.entries.forEach { slot ->
            renderState.boundTexture[slot] = null
        }
        renderState.readFramebuffer = null
        renderState.writeFramebuffer = null
    }

    fun validateState() {
        check(renderStateReady) {
            "RenderState cannot be used now: not ready!"
        }

        check(isStateInUse) {
            "RenderState cannot be used now: not acquired!"
        }

        check(executor.isOnRenderThread()) {
            "RenderState cannot be used now: wrong thread!"
        }
    }

    fun releaseState() {
        isStateInUse = false
    }

    fun begin() {
        prevTextureSlot = glGetInteger(GL_ACTIVE_TEXTURE)
        prevReadFramebuffer = glGetInteger(GL_READ_FRAMEBUFFER)
        prevWriteFramebuffer = glGetInteger(GL_DRAW_FRAMEBUFFER)
        prevVertexArrayObject = glGetInteger(GL_VERTEX_ARRAY_BINDING)

        TextureSlot.entries.forEach { slot ->
            glActiveTexture(slot.textureSlot)
            prevBoundTextures[slot] = glGetInteger(GL_TEXTURE_BINDING_2D)
        }

        glActiveTexture(TextureSlot.Slot0.textureSlot)
        glBindVertexArray(vao)
        renderStateReady = true
    }

    fun end() {
        renderStateReady = false
        prevBoundTextures.entries.forEach { (slot, id) ->
            glActiveTexture(slot.textureSlot)
            glBindTexture(GL_TEXTURE_2D, id ?: 0)
        }

        glActiveTexture(prevTextureSlot)
        glBindFramebuffer(GL_READ_FRAMEBUFFER, prevReadFramebuffer)
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, prevWriteFramebuffer)
        glBindVertexArray(prevVertexArrayObject)
    }
}