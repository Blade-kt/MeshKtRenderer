package me.blade.meshkt.renderer.state

import me.blade.meshkt.renderer.objects.framebuffer.Framebuffer
import me.blade.meshkt.renderer.objects.shader.Shader
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.util.ObservableMap.Companion.observableMap
import me.blade.meshkt.renderer.util.glGetDoubleRange
import me.blade.meshkt.renderer.util.glGetInt4
import org.lwjgl.opengl.GL45C.*

class StateManager {
    val readFramebufferState = TrackedState.create(
        originalGetter = { glGetInteger(GL_READ_FRAMEBUFFER_BINDING) },
        stateApplier = { glBindFramebuffer(GL_READ_FRAMEBUFFER, it) },
        mapToNative = { it?.id ?: 0 },
        mapToImpl = { null as Framebuffer? }
    )

    val writeFramebufferState = TrackedState.create(
        originalGetter = { glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING) },
        stateApplier = { glBindFramebuffer(GL_DRAW_FRAMEBUFFER, it) },
        mapToNative = { it?.id ?: 0 },
        mapToImpl = { null as Framebuffer? }
    )

    val boundShaderState = TrackedState.create(
        originalGetter = { glGetInteger(GL_CURRENT_PROGRAM) },
        stateApplier = { glUseProgram(it) },
        mapToNative = { it?.id ?: 0 },
        mapToImpl = { null as Shader? }
    )

    val viewportState = TrackedState.create(
        originalGetter = { glGetInt4(GL_VIEWPORT) },
        stateApplier = { glViewport(it.x, it.y, it.z, it.w) },
    )

    var activeTextureState = TrackedState.createEnum<TextureSlot>(GL_ACTIVE_TEXTURE, ::glActiveTexture)
    private val prevBoundTextures = observableMap<TextureSlot, Int>()

    var vertexArrayObjectState = TrackedState.create(
        originalGetter = { glGetInteger(GL_VERTEX_ARRAY_BINDING) },
        stateApplier = { glBindVertexArray(it) }
    )

    var depthTestState = TrackedState.createToggleStateBoolean(GL_DEPTH_TEST)
    var depthMaskState = TrackedState.createParameterStateBoolean(GL_DEPTH_WRITEMASK, ::glDepthMask)
    var depthFuncState = TrackedState.createEnum<Func>(GL_DEPTH_FUNC, ::glDepthFunc)
    var depthRangeState = TrackedState.create(
        originalGetter = { glGetDoubleRange(GL_DEPTH_RANGE) },
        stateApplier = { glDepthRange(it.start, it.endInclusive) },
    )
    var depthClampState = TrackedState.createToggleStateBoolean(GL_DEPTH_CLAMP)

    private val stateTrackers = listOf(
        readFramebufferState, writeFramebufferState,
        boundShaderState, viewportState,
        activeTextureState, vertexArrayObjectState,
        depthTestState, depthMaskState, depthFuncState, depthRangeState, depthClampState
    )

    fun begin() {
        stateTrackers.forEach(TrackedState<*, *>::begin)

        val prevValue = activeTextureState.value
        TextureSlot.reversedEntries.forEach { slot ->
            activeTextureState.apply(slot)
            prevBoundTextures[slot] = glGetInteger(GL_TEXTURE_BINDING_2D)
        }
        activeTextureState.apply(prevValue)
    }

    fun end() {
        prevBoundTextures.entries.forEach { (slot, id) ->
            activeTextureState.apply(slot)
            glBindTexture(GL_TEXTURE_2D, id!!)
        }

        stateTrackers.forEach(TrackedState<*, *>::end)
    }
}