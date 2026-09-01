package me.blade.meshkt.renderer.objects.texture

import me.blade.meshkt.renderer.objects.ObjectHandle
import me.blade.meshkt.renderer.objects.buffer.properties.BufferAccess
import me.blade.meshkt.renderer.objects.texture.groups.TextureFiltering
import me.blade.meshkt.renderer.objects.texture.groups.TextureMipmapping
import me.blade.meshkt.renderer.objects.texture.groups.TextureStorage
import me.blade.meshkt.renderer.objects.texture.groups.TextureWrapping
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import org.lwjgl.opengl.ARBBindlessTexture.glGetTextureHandleARB
import org.lwjgl.opengl.ARBBindlessTexture.glMakeTextureHandleNonResidentARB
import org.lwjgl.opengl.ARBBindlessTexture.glMakeTextureHandleResidentARB
import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11C.glDeleteTextures
import org.lwjgl.opengl.GL42C.glBindImageTexture
import org.lwjgl.opengl.GL45C.glCreateTextures

class Texture(externalId: Int?) : ObjectHandle(
    externalId ?: glCreateTextures(GL_TEXTURE_2D),
    externalId != null
) {
    val filtering = TextureFiltering(this)
    val wrapping = TextureWrapping(this)
    val mipmap = TextureMipmapping(this)
    val storage = TextureStorage(this)

    private var _bindlessHandle: Long? = null
    val bindlessHandle: Long get() {
        _bindlessHandle?.let { return it }
        val handle = glGetTextureHandleARB(id)
            .apply(::glMakeTextureHandleResidentARB)
        _bindlessHandle = handle
        return handle
    }

    var attachmentLevel = 0

    fun filtering(block: TextureFiltering.() -> Unit) = block(filtering)
    fun wrapping(block: TextureWrapping.() -> Unit) = block(wrapping)
    fun mipmap(block: TextureMipmapping.() -> Unit) = block(mipmap)
    fun storage(block: TextureStorage.() -> Unit) = block(storage)

    fun computeShaderBinding(slot: TextureSlot, access: BufferAccess, level: Int = 0, layered: Boolean = false, layer: Int = 0) {
        val internal = storage.internalFormat ?: throw IllegalStateException("Texture internalFormat is not specified")
        glBindImageTexture(slot.unitIndex, id, level, layered, layer, access.gl, internal.gl)
    }

    override fun delete() {
        _bindlessHandle?.let(::glMakeTextureHandleNonResidentARB)
        glDeleteTextures(id)
    }
}