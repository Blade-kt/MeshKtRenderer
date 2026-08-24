package me.blade.meshkt.renderer.objects.texture.groups

import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureInternalFormat
import me.blade.meshkt.renderer.objects.texture.properties.TexturePixelFormat
import me.blade.meshkt.renderer.objects.texture.properties.TexturePixelType
import org.lwjgl.opengl.GL45C.*

class TextureStorage(texture: Texture) {
    private val handle = texture.handle

    var levels: Int = 1

    var width: Int? = null
    var height: Int? = null

    var internalFormat: TextureInternalFormat? = null
    var uploadPixelFormat: TexturePixelFormat? = null
    var uploadPixelType: TexturePixelType? = null

    fun allocate() {
        val internal = internalFormat ?: throw IllegalStateException("allocate(): Texture internalFormat is not specified")

        val w = width ?: throw IllegalStateException("allocate(): Texture width is not specified")
        val h = height ?: throw IllegalStateException("allocate(): Texture height is not specified")
        check(w > 0) { "allocate(): Width must be > 0" }
        check(h > 0) { "allocate(): Height must be > 0" }
        glTextureStorage2D(handle.id, levels, internal.gl, w, h)
    }

    fun upload(
        pointer: Long,
        level: Int,
        pixelFormat: TexturePixelFormat?,
        pixelType: TexturePixelType?,
        width: Int = this.width ?: -1, height: Int = this.height ?: -1,
        offsetX: Int = 0, offsetY: Int = 0,
    ) {
        val pf = pixelFormat ?: this.uploadPixelFormat ?: throw IllegalStateException("upload(): Texture uploadPixelFormat is not specified")
        val pt = pixelType ?: this.uploadPixelType ?: throw IllegalStateException("upload(): Texture uploadPixelType is not specified")

        check(width > 0) { "upload(): Width must be > 0" }
        check(height > 0) { "upload(): Height must be > 0" }
        glTextureSubImage2D(handle.id, level, offsetX, offsetY, width, height, pf.gl, pt.gl, pointer)
    }
}