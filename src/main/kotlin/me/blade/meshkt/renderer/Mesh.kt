@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")

package me.blade.meshkt.renderer
import me.blade.meshkt.renderer.renderpass.RenderContextBridge
import me.blade.meshkt.renderer.threading.PullingStrategy
import me.blade.meshkt.renderer.threading.RenderThreadExecutor
import me.blade.meshkt.renderer.util.MeshDslObj3ct
import java.util.logging.Logger

@MeshDslObj3ct
fun createRenderThreadExecutor(
    pullingStrategy: PullingStrategy = PullingStrategy.Allocative,
    logger: Logger
) = RenderThreadExecutor(pullingStrategy, logger)

@MeshDslObj3ct
fun createRenderContext(executor: RenderThreadExecutor) = RenderContextBridge(executor)