package me.blade.meshkt.renderer.util

import java.util.EnumMap
import java.util.EnumSet

class ObservableEnumMap<K : Enum<K>, V>(
    enumClass: Class<K>,
    private val update: (K, V?) -> Unit
) {
    private val map = EnumMap(EnumSet.allOf(enumClass).associateWith { null as V? })
    val entries get() = map.entries

    operator fun get(key: K): V? = map.getValue(key) // throwy, but shouldn't happen

    operator fun set(key: K, value: V) {
        //val prevValue = get(key)
        //if (prevValue == value) return
        map[key] = value
        update(key, value)
    }

    fun forceUpdateAll() {
        map.keys.forEach { key ->
            update(key, get(key))
        }
    }

    companion object {
        inline fun <reified K : Enum<K>, V> observableEnumMap(
            noinline update: (K, V?) -> Unit,
        ) = ObservableEnumMap(K::class.java, update)
    }
}