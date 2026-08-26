@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
package me.blade.meshkt.renderer.objects.texture

import me.blade.meshkt.renderer.util.MeshDslObj3ct
import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL45.glCreateTextures
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@MeshDslObj3ct
@OptIn(ExperimentalContracts::class)
inline fun createTexture(
    handle: TextureHandle = createTextureHandle(),
    block: Texture.() -> Unit = {}
): Texture {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val texture = Texture(handle)
    block(texture)
    return texture
}

@MeshDslObj3ct
fun createTextureHandle() = TextureHandle(glCreateTextures(GL_TEXTURE_2D), false)

@MeshDslObj3ct
fun externalTextureHandle(id: Int) = TextureHandle(id, true)