package me.blade.meshkt.renderer.objects

import me.blade.meshkt.renderer.util.IMeshResource

abstract class ObjectHandle(
    private val identifier: Int,
    protected val isExternal: Boolean,
) : IMeshResource {
    var isValid = true; private set
    val id: Int get() {
        check(isValid) {
            "Instance of ${this.javaClass.simpleName}: ObjectHandle was deleted and cannot be used anymore."
        }
        return identifier
    }

    fun invalidate() {
        isValid = false
    }

    final override fun free() {
        if (!isExternal) {
            delete()
        }
        invalidate()
    }

    protected abstract fun delete()
}