package me.blade.meshkt.renderer.state

import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class TrackedState <NativeType: Any, ImplType> (
    private val originalGetter: () -> NativeType,
    private val stateApplier: (NativeType) -> Unit,
    private val mapToNative: (ImplType) -> NativeType,
    private val mapToImpl: (NativeType) -> ImplType,
) {
    private var cachedValue by Delegates.notNull<NativeType>()
    private var actualValue by Delegates.notNull<NativeType>()
    private var value: ImplType = mapToImpl(originalGetter())

    operator fun getValue(obj: Any, property: KProperty<*>) = value
    operator fun setValue(obj: Any, property: KProperty<*>, newValue0: ImplType) = setValue(newValue0)

    private fun setValue(newValue: ImplType) {
        value = newValue
        bind(mapToNative(newValue))
    }

    fun begin() {
        actualValue = originalGetter()
        cachedValue = actualValue
        value = mapToImpl(actualValue)
    }

    fun end() {
        bind(cachedValue)
    }

    private fun bind(v: NativeType) {
        if (actualValue == v) return
        actualValue = v
        stateApplier(v)
    }

    companion object {
        fun <S: Any, T> create(
            originalGetter: () -> S,
            stateApplier: (S) -> Unit,
            mapToNative: (T) -> S,
            mapToImpl: (S) -> T,
        ) = TrackedState(originalGetter, stateApplier, mapToNative, mapToImpl)
    }
}