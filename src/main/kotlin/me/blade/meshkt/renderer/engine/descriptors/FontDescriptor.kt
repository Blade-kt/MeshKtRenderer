package me.blade.meshkt.renderer.engine.descriptors

import me.blade.meshkt.renderer.util.vec.Vec2

interface IFontDescriptor {
    var pos: Vec2
    var height: Double
    var text: String
}

class FontDescriptor : IFontDescriptor {
    override var pos = Vec2.ZERO
    override var height = 0.0
    override var text = ""

    fun reset() {
        pos = Vec2.ZERO
        height = 0.0
        text = ""
    }
}