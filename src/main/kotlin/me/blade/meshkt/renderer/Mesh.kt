package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.state.StateManager
import me.blade.meshkt.renderer.util.ObservableMap.Companion.observableMap
import org.lwjgl.opengl.GL45C.*

object Mesh {
    private val stateManager = StateManager()
    private val vao by lazy(::glCreateVertexArrays)

    var readFramebuffer by stateManager.readFramebufferState
    var writeFramebuffer by stateManager.writeFramebufferState
    var boundShader by stateManager.boundShaderState
    var viewport by stateManager.viewportState
    var activeTexture by stateManager.activeTextureState
    var vertexArrayObject by stateManager.vertexArrayObjectState

    var depthTest by stateManager.depthTestState
    var depthMask by stateManager.depthMaskState
    var depthFunc by stateManager.depthFuncState
    var depthRange by stateManager.depthRangeState
    var depthClamp by stateManager.depthClampState

    val boundTexture = observableMap<TextureSlot, Texture?> { slot, texture ->
        glBindTextureUnit(slot.unitIndex, texture?.id ?: 0)
    }

    fun begin() {
        stateManager.begin()
        vertexArrayObject = vao
    }

    fun end() {
        stateManager.end()
    }

    fun dispatchCompute(numGroupsX: Int, numGroupsY: Int, numGroupsZ: Int = 1) {
        glDispatchCompute(numGroupsX, numGroupsY, numGroupsZ)
    }

    fun memoryBarrier(barrierBitMask: Int) {
        glMemoryBarrier(barrierBitMask)
    }

    fun render(
        instanceCount: Int,
        instanceSize: Int = 6
    ) {
        val shader = boundShader ?: throw IllegalStateException("Shader is not set")
        shader.storage.applyBindings()

        val vertexCount = instanceCount * instanceSize
        if (vertexCount <= 0) return
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)
    }
}