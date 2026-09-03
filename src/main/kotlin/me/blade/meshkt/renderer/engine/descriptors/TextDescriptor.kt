package me.blade.meshkt.renderer.engine.descriptors

import me.blade.meshkt.renderer.util.vec.Vec2
import java.awt.Font

interface ITextDescriptor {
    var font: Font?
    var pos: Vec2
    var height: Double
    var content: String

    fun reset()
}

class TextDescriptor : ITextDescriptor {
    override var font = null as Font?
    override var pos = Vec2.ZERO
    override var height = 0.0
    override var content = ""

    override fun reset() {
        pos = Vec2.ZERO
        height = 0.0
        content = ""
    }
}