package me.blade.meshkt.renderer.resource

import kotlinx.coroutines.launch
import me.blade.meshkt.renderer.MeshEngine
import me.blade.meshkt.renderer.util.invokePrivate

class ResourceFactory(
    val engine: MeshEngine
) {
    val resources = mutableListOf<MeshResource>()

    inline fun <reified T: MeshResource> registerResource(resource: T) {
        synchronized(resources) {
            resources.add(resource)
        }
    }

    inline fun <reified T: MeshResource> freeResource(resource: T) {
        synchronized(resources) {
            resources.remove(resource)

            engine.dispatcher.scope.launch {
                invokePrivate(resource, "free")
            }
        }
    }
}
