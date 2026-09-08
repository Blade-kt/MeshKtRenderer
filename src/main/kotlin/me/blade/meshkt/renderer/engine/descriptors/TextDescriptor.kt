package me.blade.meshkt.renderer.engine.descriptors

import me.blade.meshkt.renderer.util.vec.Vec2
import java.awt.Color
import java.awt.Font

interface ITextDescriptor {
    var font: Font?
    var pos: Vec2?
    var height: Double?
    var content: String?
    var color: Color

    fun reset()
}

class TextDescriptor : ITextDescriptor {
    override var font = null as Font?
    override var pos = null as Vec2?
    override var height = null as Double?
    override var content = null as String?
    override var color = Color.WHITE!!

    override fun reset() {
        font = null
        pos = null
        height = null
        content = null
        color = Color.WHITE!!
    }
}