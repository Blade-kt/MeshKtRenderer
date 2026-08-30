package me.blade.meshkt.renderer.objects.texture

import me.blade.meshkt.renderer.objects.ObjectHandle
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.objects.texture.groups.TextureFiltering
import me.blade.meshkt.renderer.objects.texture.groups.TextureMipmapping
import me.blade.meshkt.renderer.objects.texture.groups.TextureStorage
import me.blade.meshkt.renderer.objects.texture.groups.TextureWrapping
import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11C.glDeleteTextures
import org.lwjgl.opengl.GL45C.glCreateTextures

class Texture(externalId: Int?) : ObjectHandle(
    externalId ?: glCreateTextures(GL_TEXTURE_2D),
    externalId != null
) {
    val filtering = TextureFiltering(this)
    val wrapping = TextureWrapping(this)
    val mipmap = TextureMipmapping(this)
    val storage = TextureStorage(this)

    var attachmentLevel = 0

    fun filtering(block: TextureFiltering.() -> Unit) = block(filtering)
    fun wrapping(block: TextureWrapping.() -> Unit) = block(wrapping)
    fun mipmap(block: TextureMipmapping.() -> Unit) = block(mipmap)
    fun storage(block: TextureStorage.() -> Unit) = block(storage)

    override fun delete() {
        glDeleteTextures(id)
    }
}