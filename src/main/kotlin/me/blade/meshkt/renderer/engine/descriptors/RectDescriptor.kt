package me.blade.meshkt.renderer.engine.descriptors

import me.blade.meshkt.renderer.util.vec.Vec2
import java.awt.Color

interface IRectDescriptor {
    var pos1: Vec2
    var pos2: Vec2
    var color: Color
}

class RectDescriptor : IRectDescriptor {
    override var pos1 = Vec2.ZERO
    override var pos2 = Vec2.ZERO
    override var color = Color.WHITE

    fun reset() {
        pos1 = Vec2.ZERO
        pos2 = Vec2.ZERO
        color = Color.WHITE
    }
}