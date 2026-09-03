package me.blade.meshkt.renderer.state

import me.blade.meshkt.renderer.Mesh.vertexArrayObject
import me.blade.meshkt.renderer.objects.framebuffer.Framebuffer
import me.blade.meshkt.renderer.objects.shader.Shader
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.util.ObservableMap.Companion.observableMap
import me.blade.meshkt.renderer.util.glGetDoubleRange
import me.blade.meshkt.renderer.util.glGetFloat4
import me.blade.meshkt.renderer.util.glGetInt4
import org.lwjgl.opengl.GL45C.*

class StateManager {
    // even if you use pure ssbo-driven renderer,
    // you need any empty VAO to be bound to avoid shit with some drivers
    private val vao by lazy(::glCreateVertexArrays)

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

    var vertexArrayObjectState = TrackedState.create(
        originalGetter = { glGetInteger(GL_VERTEX_ARRAY_BINDING) },
        stateApplier = { glBindVertexArray(it) }
    )

    val viewportState = TrackedState.create(
        originalGetter = { glGetInt4(GL_VIEWPORT) },
        stateApplier = { glViewport(it.x, it.y, it.z, it.w) },
    )

    var activeTextureState = TrackedState.createEnum<TextureSlot>(GL_ACTIVE_TEXTURE, ::glActiveTexture)
    private val prevBoundTextures = observableMap<TextureSlot, Int>()
    val boundTexture = observableMap<TextureSlot, Texture?> { slot, texture ->
        glBindTextureUnit(slot.unitIndex, texture?.id ?: 0)
    }

    var depthTestState = TrackedState.createToggleStateBoolean(GL_DEPTH_TEST)
    var depthMaskState = TrackedState.createParameterStateBoolean(GL_DEPTH_WRITEMASK, ::glDepthMask)
    var depthFuncState = TrackedState.createEnum<Func>(GL_DEPTH_FUNC, ::glDepthFunc)
    var depthRangeState = TrackedState.create(
        originalGetter = { glGetDoubleRange(GL_DEPTH_RANGE) },
        stateApplier = { glDepthRange(it.start, it.endInclusive) },
    )
    var depthClampState = TrackedState.createToggleStateBoolean(GL_DEPTH_CLAMP)

    var blendState = TrackedState.createToggleStateBoolean(GL_BLEND)
    var blendFuncState = TrackedState.create(BlendFunc::fromGL, BlendFunc::apply)
    var blendEquationState = TrackedState.create(BlendEquation::fromGL, BlendEquation::apply)
    var blendColorState = TrackedState.create(
        originalGetter = { glGetFloat4(GL_BLEND_COLOR) },
        stateApplier = { glBlendColor(it.x.toFloat(), it.y.toFloat(), it.z.toFloat(), it.w.toFloat()) },
    )
    var colorMaskState = TrackedState.create(ColorMask::fromGL, ColorMask::apply)

    private val stateTrackers = listOf(
        readFramebufferState, writeFramebufferState,
        boundShaderState, vertexArrayObjectState,
        viewportState, activeTextureState,
        depthTestState, depthMaskState, depthFuncState, depthRangeState, depthClampState,
        blendState, blendFuncState, blendEquationState, blendColorState, colorMaskState
    )

    private var stateReady = false

    fun begin() {
        stateTrackers.forEach(TrackedState<*, *>::begin)

        TextureSlot.entries.forEach { slot ->
            activeTextureState.apply(slot)
            prevBoundTextures[slot] = glGetInteger(GL_TEXTURE_BINDING_2D)
        }

        activeTextureState.apply(TextureSlot.Slot0)
        vertexArrayObject = vao
        stateReady = true
    }

    fun end() {
        stateReady = false
        prevBoundTextures.entries.forEach { (slot, id) ->
            activeTextureState.apply(slot)
            glBindTexture(GL_TEXTURE_2D, id!!)
        }

        stateTrackers.forEach(TrackedState<*, *>::end)
    }

    fun <R> ensureStateSetup(block: () -> R): R {
        if (stateReady) {
            return block()
        }

        begin()
        val returnValue = block()
        end()

        return returnValue
    }

    fun validate() {
        check(stateReady) {
            "OpenGL state is not setup. Consider using it within Mesh::begin - Mesh::end calls."
        }
    }
}