package me.blade.meshkt.renderer.objects.framebuffer

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.util.vec.Vec2i

class ViewportFramebuffer : Framebuffer(null) {
    private var lastViewportSize: Vec2i? = null

    fun updateAndBind(viewportSize: Vec2i) {
        Mesh.writeFramebuffer = this

        if (lastViewportSize == viewportSize) return
        lastViewportSize = viewportSize
    }
}