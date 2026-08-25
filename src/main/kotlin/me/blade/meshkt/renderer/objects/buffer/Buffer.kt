package me.blade.meshkt.renderer.objects.buffer

import me.blade.meshkt.renderer.resource.IMeshResource

class Buffer(val handle: BufferHandle) : IMeshResource {


    override fun free() {
        handle.free()
    }
}