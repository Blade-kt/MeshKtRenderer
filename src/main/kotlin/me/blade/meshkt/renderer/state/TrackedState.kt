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
    private class NullableValue <T> (var value: T)

    // value that was set outside mesh context
    private var cachedValue by Delegates.notNull<NativeType>()

    // value that represents actual gl state (if the impl is correct)
    private var actualValue by Delegates.notNull<NativeType>()

    // mapped value that represents actual gl state
    private var valueStorage: NullableValue<ImplType>? = null
    private var initialized = false

    operator fun getValue(obj: Any, property: KProperty<*>): ImplType {
        check(initialized) {
            "State is not initialized"
        }

        val storage = valueStorage
            ?: throw IllegalStateException("Value storage is not initialized. This kind of message should not appear")

        return storage.value
    }
    operator fun setValue(obj: Any, property: KProperty<*>, newValue0: ImplType) {
        apply(newValue0)
    }

    fun apply(newValue: ImplType) {
        check(initialized) {
            "State is not initialized"
        }

        valueStorage = NullableValue(newValue)
        bind(mapToNative(newValue))
    }

    fun capture() {
        check(!initialized) {
            "State is already captured"
        }

        actualValue = originalGetter()
        cachedValue = actualValue
        valueStorage = NullableValue(mapToImpl(actualValue))
        initialized = true
    }

    fun revert() {
        check(initialized) {
            "State is not initialized"
        }

        bind(cachedValue)
        initialized = false
        valueStorage = null
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

        fun createToggleBoolean(
            key: Int
        ) = createBoolean(
            { glIsEnabled(key) },
            { if (it) glEnable(key) else glDisable(key) },
        )

        fun createParameterBoolean(
            key: Int,
            setter: (Boolean) -> Unit
        ) = createBoolean(
            { glGetBoolean(key) },
            { setter(it) },
        )

        fun createParameterInt(
            key: Int,
            setter: (Int) -> Unit
        ) = create(
            { glGetInteger(key) },
            { setter(it) },
        )

        fun createBoolean(
            getter: () -> Boolean,
            setter: (Boolean) -> Unit
        ) = create(
            { getter() },
            { setter(it) },
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