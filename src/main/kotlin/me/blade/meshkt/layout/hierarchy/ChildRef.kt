package me.blade.meshkt.layout.hierarchy

import me.blade.meshkt.layout.Layout

class ChildRef(
    val update: UpdateConvention,
    val updateMethod: () -> Collection<Layout>,
) {
    var isFirstUpdate = true
    val childrenGroup = mutableListOf<Layout>()

    private var prevDependencyValues: Array<Any?>? = null

    fun update() {
        val rebuild = run {
            when (val casted = update) {
                UpdateConvention.Static -> {
                    isFirstUpdate
                }
                is UpdateConvention.Conditional -> {
                    isFirstUpdate || casted.shouldUpdate()
                }
                is UpdateConvention.ByDependencies -> {
                    // capture prev & get current & set prev
                    val lastDependencyValues = prevDependencyValues?.map { it }?.toTypedArray() ?: emptyArray()
                    val newDependencyValues = casted.dependencyArray.map { it.invoke() }.toTypedArray()
                    prevDependencyValues = newDependencyValues

                    if (lastDependencyValues.size != newDependencyValues.size) {
                        return@run true // first update
                    }

                    if (!areDepsEqual(lastDependencyValues, newDependencyValues)) {
                        return@run true
                    }

                    return@run false
                }
            }
        }

        if (rebuild) {
            rebuild()
        }
    }

    fun rebuild() {
        childrenGroup.clear()
        childrenGroup.addAll(updateMethod())
        isFirstUpdate = false
    }

    private fun areDepsEqual(dep1: Any?, dep2: Any?): Boolean {
        return when (dep1) {
            is Collection<*> if dep2 is Collection<*> -> {
                if (dep1.size != dep2.size) return false
                dep1.zip(dep2).all { (first, second) -> areDepsEqual(first, second) }
            }

            is Array<*> if dep2 is Array<*> -> {
                if (dep1.size != dep2.size) return false
                dep1.zip(dep2).all { (first, second) -> areDepsEqual(first, second) }
            }

            else -> dep1 == dep2
        }
    }
}