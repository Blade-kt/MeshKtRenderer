package me.blade.meshkt.renderer.pipeline

import me.blade.meshkt.renderer.renderpass.RenderState
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11C.glDrawArrays

class RenderPass(val state: RenderState) {
    var instanceSize = 6
    var instanceCount = 0

    fun render() {
        val vertexCount = instanceCount * instanceSize
        if (vertexCount <= 0) return

        state.validate()
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)
    }

    companion object {

    }
}