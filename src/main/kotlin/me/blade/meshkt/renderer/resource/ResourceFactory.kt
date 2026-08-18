package me.blade.meshkt.renderer.resource

import kotlinx.coroutines.Job

class ResourceFactory {
    private val resources = mutableListOf<IMeshResource>()

    fun <T: IMeshResource> registerResource(resource: T) = resourceMutex {
        resources.add(resource)
    }
    
    fun completeDeferredOperations() {
        
    }

    private fun resourceMutex(block: () -> Unit) {
        synchronized(resources) {
            block()
        }
    }
}