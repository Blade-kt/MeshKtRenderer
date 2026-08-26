@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
package me.blade.meshkt.renderer.objects.ssbo

import me.blade.meshkt.renderer.util.MeshDslObj3ct
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@MeshDslObj3ct
@OptIn(ExperimentalContracts::class)
inline fun createShaderStorageBuffer(
    size: Long,
    block: ShaderStorageBuffer.() -> Unit = {}
): ShaderStorageBuffer {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val buffer = ShaderStorageBuffer(size)
    block(buffer)
    return buffer
}