@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
package me.blade.meshkt.renderer.pipeline

import me.blade.meshkt.renderer.renderpass.RenderContext
import me.blade.meshkt.renderer.util.MeshDslObj3ct

@MeshDslObj3ct
fun renderPass(
    context: RenderContext,
    block: RenderPass.() -> Unit
) {
    val state = context.acquireRenderState()

    val pipeline = RenderPass(state)
    block(pipeline)
    pipeline.render()

    state.release()
}