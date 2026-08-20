package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.resource.ResourceFactory
import me.blade.meshkt.renderer.threading.GLThreadDispatcher

class MeshEngine(val dispatcher: GLThreadDispatcher) {
    val resourceFactory = ResourceFactory(this)
}