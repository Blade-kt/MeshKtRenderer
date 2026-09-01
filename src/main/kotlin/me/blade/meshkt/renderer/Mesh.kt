package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.objects.framebuffer.Framebuffer
import me.blade.meshkt.renderer.objects.shader.Shader
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.state.TrackedState
import me.blade.meshkt.renderer.util.ObservableMap.Companion.observableMap
import me.blade.meshkt.renderer.util.Quad
import org.lwjgl.opengl.GL45C.*

object Mesh {
    private val vao by lazy(::glCreateVertexArrays)

    private var prevTextureSlot = TextureSlot.Slot0.textureSlot
    private val prevBoundTextures = observableMap<TextureSlot, Int>()
    private var prevVertexArrayObject = 0

    private val readFramebufferState = TrackedState.create(
        originalGetter = { glGetInteger(GL_READ_FRAMEBUFFER_BINDING) },
        stateApplier = { glBindFramebuffer(GL_READ_FRAMEBUFFER, it) },
        mapToNative = { it?.id ?: 0 },
        mapToImpl = { null as Framebuffer? }
    )

    private val writeFramebufferState = TrackedState.create(
        originalGetter = { glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING) },
        stateApplier = { glBindFramebuffer(GL_DRAW_FRAMEBUFFER, it) },
        mapToNative = { it?.id ?: 0 },
        mapToImpl = { null as Framebuffer? }
    )

    private val boundShaderState = TrackedState.create(
        originalGetter = { glGetInteger(GL_CURRENT_PROGRAM) },
        stateApplier = { glUseProgram(it) },
        mapToNative = { it?.id ?: 0 },
        mapToImpl = { null as Shader? },
    )

    private val viewportState = TrackedState.create(
        originalGetter = { IntArray(4).also { glGetIntegerv(GL_VIEWPORT, it) }.let { Quad(it[0], it[1], it[2], it[3]) } },
        stateApplier = { glViewport(it.first, it.second, it.third, it.fourth) },
        mapToNative = { it },
        mapToImpl = { it },
    )

    private val stateTrackers = listOf(readFramebufferState, writeFramebufferState, boundShaderState, viewportState)

    var readFramebuffer by readFramebufferState
    var writeFramebuffer by writeFramebufferState
    var boundShader by boundShaderState
    val boundTexture = observableMap<TextureSlot, Texture?> { slot, texture ->
        glBindTextureUnit(slot.unitIndex, texture?.id ?: 0)
    }

    fun begin() {
        stateTrackers.forEach(TrackedState<*, *>::begin)
        prevTextureSlot = glGetInteger(GL_ACTIVE_TEXTURE)
        prevVertexArrayObject = glGetInteger(GL_VERTEX_ARRAY_BINDING)

        TextureSlot.reversedEntries.forEach { slot ->
            glActiveTexture(slot.textureSlot)
            prevBoundTextures[slot] = glGetInteger(GL_TEXTURE_BINDING_2D)
        }

        glBindVertexArray(vao)
    }

    fun end() {
        stateTrackers.forEach(TrackedState<*, *>::end)

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