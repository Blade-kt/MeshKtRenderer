package me.blade.meshkt.renderer.resource

import me.blade.meshkt.renderer.MeshEngine
import me.blade.meshkt.renderer.threading.ExecutionStrategy
import me.blade.meshkt.renderer.util.invokePrivate

class ResourceFactory(
    val engine: MeshEngine
) {
    val resources = mutableListOf<IMeshResource>()

    inline fun <reified T: IMeshResource> registerResource(resource: T) {
        engine.dispatcher.launch(strategy = ExecutionStrategy.Adaptive) {
            resources.add(resource)
        }
    }

    inline fun <reified T: IMeshResource> freeResource(resource: T) {
        engine.dispatcher.launch(strategy = ExecutionStrategy.Adaptive) {
            resources.remove(resource)
            invokePrivate(resource, "free")
        }
    }
}
