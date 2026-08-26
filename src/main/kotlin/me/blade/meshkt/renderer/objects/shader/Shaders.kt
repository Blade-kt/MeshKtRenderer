@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
package me.blade.meshkt.renderer.objects.shader

import me.blade.meshkt.renderer.util.MeshDslObj3ct
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
@MeshDslObj3ct
inline fun createShader(
    block: Shader.() -> Unit
): Shader {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val shader = Shader()
    block(shader)
    shader.link()

    return shader
}