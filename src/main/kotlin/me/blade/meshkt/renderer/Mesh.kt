package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.engine.IRenderer
import me.blade.meshkt.renderer.objects.createShader
import me.blade.meshkt.renderer.objects.externalFramebuffer
import me.blade.meshkt.renderer.objects.shader.Shader
import me.blade.meshkt.renderer.state.StateManager
import me.blade.meshkt.renderer.threading.PullingStrategy
import me.blade.meshkt.renderer.threading.RenderThreadExecutor
import org.lwjgl.opengl.GL45C.*

object Mesh {
    private val stateManager = StateManager()
    val executor = RenderThreadExecutor(PullingStrategy.Allocative)

    var readFramebuffer by stateManager.readFramebufferState
    var writeFramebuffer by stateManager.writeFramebufferState
    var boundShader by stateManager.boundShaderState
    var vertexArrayObject by stateManager.vertexArrayObjectState
    var viewport by stateManager.viewportState

    var activeTexture by stateManager.activeTextureState
    val boundTexture by stateManager::boundTexture

    var depthTest by stateManager.depthTestState
    var depthMask by stateManager.depthMaskState
    var depthFunc by stateManager.depthFuncState
    var depthRange by stateManager.depthRangeState
    var depthClamp by stateManager.depthClampState

    var blend by stateManager.blendState
    var blendFunc by stateManager.blendFuncState
    var blendEquation by stateManager.blendEquationState
    var blendColor by stateManager.blendColorState
    var colorMask by stateManager.colorMaskState

    var cull by stateManager.cullState
    var cullMode by stateManager.cullModeState
    var frontFace by stateManager.frontFaceState

    val defaultFramebufferAccess = externalFramebuffer(0)

    private var activeRenderer: IRenderer? = null
    private var vertexCount = 0
    private var instanceCount = 0
    private var drawCallCount = 0
    private var frames = 0
    private var lastFramePrint = 0L

    fun signal(renderer: IRenderer) {
        signalInternal(renderer)
    }

    private fun signalInternal(renderer: IRenderer?) {
        if (renderer != null) {
            instanceCount++
        }

        if (activeRenderer == renderer) return
        val prevRenderer = activeRenderer
        activeRenderer = renderer

        prevRenderer?.flush()
    }

    fun flushRemaining() {
        signalInternal(null)
    }

    fun frameBegin() {
        executor.pollEvents()
        frames++

        val time = System.currentTimeMillis()
        if (time - lastFramePrint > 500L) {
            lastFramePrint = time
            println("Vertices: $vertexCount. Instances: $instanceCount. Draw calls: $drawCallCount. FPS: ${frames * 2}.")
            frames = 0
        }
        vertexCount = 0
        instanceCount = 0
        drawCallCount = 0
    }

    fun setupState() =
        stateManager.begin()

    fun revertState() =
        stateManager.end()

    fun <R> ensureStateSetup(block: () -> R) =
        stateManager.ensureStateSetup(block)

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
        stateManager.validate()

        boundShader = shader
        shader.storage.applyBindings()

        val vertexCount = instanceCount * instanceSize
        if (vertexCount <= 0) return
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)
        Mesh.vertexCount += vertexCount
        drawCallCount++
    }
}