package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.util.vec.Vec2

object MeshBlur : IRenderer {
    fun blur(pos1: Vec2, pos2: Vec2) {
        Mesh.signal(this)
    }

    override fun flush() {}
    override fun reset() {}
}