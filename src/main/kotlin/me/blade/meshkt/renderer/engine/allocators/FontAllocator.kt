package me.blade.meshkt.renderer.engine.allocators

import me.blade.meshkt.renderer.engine.font.FONT_SIZE
import me.blade.meshkt.renderer.engine.font.GlyphMap
import me.blade.meshkt.renderer.engine.font.buildGlyphMap
import me.blade.meshkt.renderer.objects.buffer.Buffer
import java.awt.Font

class FontAllocator(val glyphBuffer: Buffer) {
    private val glyphMapCache = hashMapOf<Font, GlyphMap>()
    private var lastGlyphIndex = 0
    private var bufferDirty = false

    fun alloc(font: Font): GlyphMap {
        val sized = font.deriveFont(FONT_SIZE)

        return glyphMapCache.getOrPut(sized) {
            bufferDirty = true
            buildGlyphMap(sized).apply {
                charData.values.forEach { glyphData ->
                    glyphBuffer.vec2(glyphData.u0, glyphData.v0)
                    glyphBuffer.vec2(glyphData.u1, glyphData.v1)
                    glyphData.index = lastGlyphIndex++
                }
            }
        }
    }

    fun flush() {
        if (!bufferDirty) return
        bufferDirty = false

        glyphBuffer.upload()
    }
}