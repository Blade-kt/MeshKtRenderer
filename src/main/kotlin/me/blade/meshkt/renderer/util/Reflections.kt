package me.blade.meshkt.renderer.util

import me.blade.meshkt.renderer.resource.MeshResource

inline fun <reified T: MeshResource> invokePrivate(instance: T, methodName: String) {
    val clazz = T::class.java
    val method = clazz.declaredMethods.first { it.name == methodName } ?: throw IllegalStateException("Method not found: $methodName")
    method.isAccessible = true
    method.invoke(instance)
    method.isAccessible = false
}