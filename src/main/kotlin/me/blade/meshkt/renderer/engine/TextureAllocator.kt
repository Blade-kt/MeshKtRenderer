package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.objects.buffer.Buffer
import me.blade.meshkt.renderer.objects.texture.Texture

class TextureAllocator(val buffer: Buffer) {
    private val cache = hashMapOf<Int, Int>()
    val textures = arrayListOf<Texture>()

    fun alloc(texture: Texture?) = texture?.let { tex ->
        cache.getOrPut(tex.id) { // IDK if using gl id as hash key is a good idea
            textures.add(tex)
            textures.lastIndex
        }
    } ?: -1

    fun reset() {
        cache.clear()
        textures.clear()
    }

    fun flush() {
        buffer.reset()
        textures.forEach { texture ->
            buffer.long(texture.bindlessHandle)
        }
        buffer.upload()
    }
}