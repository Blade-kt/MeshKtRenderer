package me.blade.meshkt.renderer.util

class ObservableMap<K, V>(
    private val update: (K, V?) -> Unit
) {
    private val map = hashMapOf<K, V?>()
    val entries get() = map.entries

    operator fun get(key: K): V? = map.getValue(key)

    operator fun set(key: K, value: V) {
        map[key] = value
        update(key, value)
    }

    companion object {
        inline fun <reified K, V> observableMap(
            noinline update: (K, V?) -> Unit = { _, _ -> },
        ) = ObservableMap(update)
    }
}