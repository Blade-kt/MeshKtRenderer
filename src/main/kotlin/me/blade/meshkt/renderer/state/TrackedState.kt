package me.blade.meshkt.renderer.state

import org.lwjgl.opengl.GL11C.*
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
    var value: ImplType = mapToImpl(originalGetter()); private set

    operator fun getValue(obj: Any, property: KProperty<*>) = value
    operator fun setValue(obj: Any, property: KProperty<*>, newValue0: ImplType) = apply(newValue0)

    fun apply(newValue: ImplType) {
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
        fun <T: Any> create(
            originalGetter: () -> T,
            stateApplier: (T) -> Unit,
        ) = TrackedState(originalGetter, stateApplier,  { it }, { it })

        fun <S: Any, T> create(
            originalGetter: () -> S,
            stateApplier: (S) -> Unit,
            mapToNative: (T) -> S,
            mapToImpl: (S) -> T,
        ) = TrackedState(originalGetter, stateApplier, mapToNative, mapToImpl)

        fun createToggleStateBoolean(
            key: Int
        ) = createBoolean(
            { glIsEnabled(key) },
            { if (it) glEnable(key) else glDisable(key) },
        )

        fun createParameterStateBoolean(
            key: Int,
            setter: (Boolean) -> Unit
        ) = createBoolean(
            { glGetBoolean(key) },
            { setter(it) },
        )

        fun createBoolean(
            getter: () -> Boolean,
            setter: (Boolean) -> Unit
        ) = create(
            { getter() },
            { setter(it) },
            { it },
            { it }
        )

        inline fun <reified E> createEnum(
            key: Int,
            crossinline setter: (Int) -> Unit,
        ) where E : Enum<E>, E : GLInt = create<Int, E>(
            { glGetInteger(key) },
            { setter(it) },
            { it.gl },
            { native -> E::class.java.enumConstants.first { it.gl == native } }
        )
    }
}