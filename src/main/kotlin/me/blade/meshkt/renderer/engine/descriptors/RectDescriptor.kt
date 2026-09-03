package me.blade.meshkt.renderer.engine.descriptors

import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.util.vec.Vec2
import java.awt.Color

interface IRectDescriptor {
    var pos1: Vec2
    var pos2: Vec2
    var color: Color
    var texture: Texture?
}

class RectDescriptor : IRectDescriptor {
    override var pos1 = Vec2.ZERO
    override var pos2 = Vec2.ZERO
    override var color = Color.WHITE!!
    override var texture: Texture? = null

    fun reset() {
        pos1 = Vec2.ZERO
        pos2 = Vec2.ZERO
        color = Color.WHITE
        texture = null
    }
}