package me.blade.meshkt.renderer.objects.texture

import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.objects.texture.groups.TextureFiltering
import me.blade.meshkt.renderer.objects.texture.groups.TextureMipmapping
import me.blade.meshkt.renderer.objects.texture.groups.TextureStorage
import me.blade.meshkt.renderer.objects.texture.groups.TextureWrapping

class Texture(
    val handle: TextureHandle
) : IMeshResource {
    val filtering = TextureFiltering(handle)
    val wrapping = TextureWrapping(handle)
    val mipmap = TextureMipmapping(handle)
    val storage = TextureStorage(handle)

    var attachmentLevel = 0

    fun filtering(block: TextureFiltering.() -> Unit) = block(filtering)
    fun wrapping(block: TextureWrapping.() -> Unit) = block(wrapping)
    fun mipmap(block: TextureMipmapping.() -> Unit) = block(mipmap)
    fun storage(block: TextureStorage.() -> Unit) = block(storage)

    override fun free() {
        handle.free()
    }
}