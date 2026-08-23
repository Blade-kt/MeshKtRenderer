package me.blade.meshkt.renderer.resource

import me.blade.meshkt.renderer.engine.MeshEngine
import java.util.List.copyOf

class ResourceFactory(
    val engine: MeshEngine
) : IMeshResource {
    private val resources = mutableListOf<IMeshResource>()
    private val synchronizationLock = this

    fun getResources() = synchronized(synchronizationLock, ::resources)

    fun <T: IMeshResource> registerResource(resource: T) {
        synchronized(synchronizationLock) {
            resources.add(resource)
        }
    }

    fun <T: IMeshResource> unregisterResource(resource: T) {
        synchronized(synchronizationLock) {
            resources.remove(resource)
        }
    }

    override fun free() {
        synchronized(synchronizationLock) {
            val resourcesCopy = copyOf(resources)
            resources.clear()

            resourcesCopy.forEach {
                it.free()
            }
        }
    }
}
