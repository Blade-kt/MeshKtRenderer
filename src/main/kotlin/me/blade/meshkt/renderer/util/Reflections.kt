package me.blade.meshkt.renderer.util

import me.blade.meshkt.renderer.resource.IMeshResource
import kotlin.reflect.KMutableProperty

inline fun <reified T: IMeshResource> invokePrivate(instance: T, methodName: String) {
    val clazz = T::class.java
    val method = clazz.declaredMethods.first { it.name == methodName } ?: throw IllegalStateException("Method not found: $methodName")
    method.isAccessible = true
    method.invoke(instance)
    method.isAccessible = false
}

fun <T> rent(property: KMutableProperty<T>, value: T, block: () -> Unit) {
    val prevValue = property.getter.call()
    property.setter.call(value)
    block()
    property.setter.call(prevValue)
}