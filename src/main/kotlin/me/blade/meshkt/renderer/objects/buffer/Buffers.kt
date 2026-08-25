@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
package me.blade.meshkt.renderer.objects.buffer

import me.blade.meshkt.renderer.util.MeshDslObj3ct
import org.lwjgl.opengl.GL45C.glCreateBuffers

@MeshDslObj3ct
fun createBuffer(
    handle: BufferHandle = createBufferHandle(),
    block: Buffer.() -> Unit = {}
): Buffer {
    val buffer = Buffer(handle)
    block(buffer)
    return buffer
}

@MeshDslObj3ct
fun createBufferHandle() = BufferHandle(glCreateBuffers(), false)

@MeshDslObj3ct
fun externalBufferHandle(id: Int) = BufferHandle(id, true)