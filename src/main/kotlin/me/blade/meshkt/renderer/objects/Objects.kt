package me.blade.meshkt.renderer.objects

import me.blade.meshkt.renderer.objects.buffer.Buffer
import me.blade.meshkt.renderer.objects.framebuffer.Framebuffer
import me.blade.meshkt.renderer.objects.shader.Shader
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.util.MeshDslObj3ct

@MeshDslObj3ct
fun createBuffer(initialCapacity: Long = 1024, block: Buffer.() -> Unit = {}) =
    Buffer(initialCapacity).apply(block)

@MeshDslObj3ct
fun createFramebuffer(block: Framebuffer.() -> Unit = {}) =
    Framebuffer(null).apply(block)

@MeshDslObj3ct
fun createShader(block: Shader.() -> Unit = {}) =
    Shader().apply(block)

@MeshDslObj3ct
fun createTexture(block: Texture.() -> Unit = {}) =
    Texture(null).apply(block)



fun externalFramebuffer(id: Int) =
    Framebuffer(id)

fun externalTexture(id: Int) =
    Texture(id)