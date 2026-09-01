package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.objects.framebuffer.Framebuffer
import me.blade.meshkt.renderer.objects.shader.Shader
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.resource.TrackedState
import me.blade.meshkt.renderer.util.ObservableMap.Companion.observableMap
import org.lwjgl.opengl.GL45C.*

object Mesh {
    private val vao by lazy(::glCreateVertexArrays)

    private var prevTextureSlot = TextureSlot.Slot0.textureSlot
    private val prevBoundTextures = observableMap<TextureSlot, Int>()
    private var prevVertexArrayObject = 0

    private val readFramebufferState = TrackedState.create<Framebuffer>(GL_READ_FRAMEBUFFER_BINDING) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, it)
    }

    private val writeFramebufferState = TrackedState.create<Framebuffer>(GL_DRAW_FRAMEBUFFER_BINDING) {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, it)
    }

    private val boundShaderState = TrackedState.create<Shader>(GL_CURRENT_PROGRAM) {
        glUseProgram(it)
    }

    private val stateTrackers = listOf(readFramebufferState, writeFramebufferState, boundShaderState)

    var readFramebuffer by readFramebufferState
    var writeFramebuffer by writeFramebufferState
    var boundShader by boundShaderState
    val boundTexture = observableMap<TextureSlot, Texture?> { slot, texture ->
        glBindTextureUnit(slot.unitIndex, texture?.id ?: 0)
    }

    fun setupState() {
        stateTrackers.forEach(TrackedState<*>::begin)
        prevTextureSlot = glGetInteger(GL_ACTIVE_TEXTURE)
        prevVertexArrayObject = glGetInteger(GL_VERTEX_ARRAY_BINDING)

        TextureSlot.reversedEntries.forEach { slot ->
            glActiveTexture(slot.textureSlot)
            prevBoundTextures[slot] = glGetInteger(GL_TEXTURE_BINDING_2D)
        }

        glBindVertexArray(vao)
    }

    fun revertState() {
        stateTrackers.forEach(TrackedState<*>::end)

        prevBoundTextures.entries.forEach { (slot, id) ->
            glActiveTexture(slot.textureSlot)
            glBindTexture(GL_TEXTURE_2D, id ?: 0)
        }

        glActiveTexture(prevTextureSlot)
        glBindVertexArray(prevVertexArrayObject)
    }

    fun dispatchCompute(numGroupsX: Int, numGroupsY: Int, numGroupsZ: Int = 1) {
        glDispatchCompute(numGroupsX, numGroupsY, numGroupsZ)
    }

    fun memoryBarrier(barrierBitMask: Int) {
        glMemoryBarrier(barrierBitMask)
    }

    fun render(
        shader: Shader,
        instanceCount: Int,
        instanceSize: Int = 6
    ) {
        boundShader = shader
        shader.storage.applyBindings()

        val vertexCount = instanceCount * instanceSize
        if (vertexCount <= 0) return
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)
    }
}