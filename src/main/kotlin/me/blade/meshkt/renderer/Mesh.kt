@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")

package me.blade.meshkt.renderer
import me.blade.meshkt.renderer.renderpass.RenderContext
import me.blade.meshkt.renderer.threading.PullingStrategy
import me.blade.meshkt.renderer.threading.RenderThreadExecutor
import me.blade.meshkt.renderer.util.MeshDslObj3ct
import java.util.logging.Logger

@MeshDslObj3ct
fun createExecutor(
    pullingStrategy: PullingStrategy = PullingStrategy.Allocative,
    logger: Logger
) = RenderThreadExecutor(pullingStrategy, logger)

@MeshDslObj3ct
fun createContext(executor: RenderThreadExecutor) = RenderContext(executor)