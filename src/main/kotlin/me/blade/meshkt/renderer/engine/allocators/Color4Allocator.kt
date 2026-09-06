package me.blade.meshkt.renderer.engine.allocators

import me.blade.meshkt.renderer.objects.buffer.Buffer
import me.blade.meshkt.renderer.util.Quad
import me.blade.meshkt.renderer.util.packColorARGB
import java.awt.Color

typealias Color4 = Quad<Color, Color, Color, Color>

class Color4Allocator(val buffer: Buffer) {
    private val cache = hashMapOf<Color4, Int>()
    val colors = arrayListOf<Color4>()

    fun alloc(color4: Color4) =
        cache.getOrPut(color4) {
            colors.add(color4)
            colors.lastIndex
        }

    fun reset() {
        cache.clear()
        colors.clear()
    }

    fun flush() {
        buffer.reset()
        colors.forEach { color ->
            buffer.ivec4(
                packColorARGB(color.first),
                packColorARGB(color.second),
                packColorARGB(color.third),
                packColorARGB(color.fourth)
            )
        }
        buffer.upload()
    }
}