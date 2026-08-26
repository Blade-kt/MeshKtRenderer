@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
package me.blade.meshkt.renderer.objects.framebuffer

import me.blade.meshkt.renderer.util.MeshDslObj3ct
import org.lwjgl.opengl.GL45C.glCreateFramebuffers
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@MeshDslObj3ct
@OptIn(ExperimentalContracts::class)
inline fun createFramebuffer(
    handle: FramebufferHandle = createFramebufferHandle(),
    validate: Boolean = true,
    block: Framebuffer.() -> Unit = {}
): Framebuffer {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val framebuffer = Framebuffer(handle)
    block(framebuffer)
    if (validate) framebuffer.validate()
    return framebuffer
}

@MeshDslObj3ct
fun createFramebufferHandle() = FramebufferHandle(glCreateFramebuffers(), false)

@MeshDslObj3ct
fun externalFramebufferHandle(id: Int) = FramebufferHandle(id, true)