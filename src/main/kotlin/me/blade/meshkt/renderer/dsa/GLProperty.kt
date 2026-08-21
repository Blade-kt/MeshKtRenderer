package me.blade.meshkt.renderer.dsa

import kotlin.reflect.KProperty

class GLProperty <T> (
    initialValue: T,
    private val setter: (T) -> Unit
) {
    private var _value: T = initialValue

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return _value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        if (_value == newValue) return
        _value = newValue

        setter(newValue)
    }

    companion object {
        fun <T> property(initialValue: T, setter: (T) -> Unit) = GLProperty(initialValue, setter)
    }
}