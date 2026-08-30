package me.blade.meshkt.renderer.resource

import me.blade.meshkt.renderer.objects.ObjectHandle
import org.lwjgl.opengl.GL11C.glGetInteger
import kotlin.reflect.KProperty

class TrackedState <T : ObjectHandle> (
    private val binding: Int,
    private val update: (Int) -> Unit
) {
    private var cachedValue: Int = 0
    private var actualValue: Int = 0
    private var value: T? = null

    operator fun getValue(obj: Any, property: KProperty<*>) = value
    operator fun setValue(obj: Any, property: KProperty<*>, newValue0: T?) = setValue(newValue0)

    private fun setValue(newValue: T?) {
        value = newValue
        bind(newValue?.id ?: 0)
    }

    fun begin() {
        actualValue = glGetInteger(binding)
        cachedValue = actualValue
        value = null
    }

    fun end() {
        bind(cachedValue)
    }

    private fun bind(v: Int) {
        if (actualValue == v) return
        actualValue = v
        update(v)
    }

    companion object {
        fun <T : ObjectHandle> create(binding: Int, update: (Int) -> Unit) =
            TrackedState<T>(binding, update)
    }
}