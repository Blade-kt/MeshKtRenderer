package me.blade.meshkt.renderer.resource

import me.blade.meshkt.renderer.MeshEngine
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
abstract class IMeshResource(protected val engine: MeshEngine) {
    abstract fun free()
}