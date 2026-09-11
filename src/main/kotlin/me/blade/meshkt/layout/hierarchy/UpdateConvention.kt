package me.blade.meshkt.layout.hierarchy

import com.sun.tools.javac.jvm.PoolConstant

/**
 * Determines when a component's children should be rebuilt.
 *
 * Controls the update strategy for child components in the reconciliation process.
 */
sealed class UpdateConvention {
    /**
     * Children are built once and never updated.
     *
     * Use for truly static content that never needs to change.
     */
    object Static : UpdateConvention()

    /**
     * Children are rebuilt whenever the condition evaluates to `true`.
     *
     * @param shouldUpdate Called before each update pass to determine if children need rebuilding
     */
    class Conditional(
        val shouldUpdate: () -> Boolean
    ) : UpdateConvention()

    /**
     * Children are rebuilt when any of the tracked dependencies change.
     *
     * Uses structural equality (`==`) to detect changes for each dependency.
     *
     * @param dependencyArray Array of tracked dependencies; rebuilds if **any** value changes
     */
    class ByDependencies(
        val dependencyArray: Array<() -> Any?>
    ) : UpdateConvention()

    companion object {
        val Dynamic = Conditional { true }
    }
}