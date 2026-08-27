@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
package me.blade.meshkt.renderer.objects.buffer

import me.blade.meshkt.renderer.util.MeshDslObj3ct
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@MeshDslObj3ct
fun createBuffer(
    initialCapacity: Long = 1024,
) = Buffer(initialCapacity)