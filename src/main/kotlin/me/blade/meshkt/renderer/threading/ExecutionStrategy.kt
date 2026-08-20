package me.blade.meshkt.renderer.threading

/**
 * Defines the execution strategy for coroutines launched through the dispatcher.
 *
 * [Adaptive] Executes immediately if called from the render thread,
 *   otherwise defers to the next frame. Best for performance when already on
 *   the correct thread.
 *
 * [Deferred] Always defers execution to the next frame, regardless of
 *   the calling thread. Useful for ensuring predictable frame-by-frame execution.
 *
 * [Lazy] (Chained) Defers actions to a separate action pool where only one
 *   action is processed per frame. Useful for long-running operations that
 *   should be spread across multiple frames to avoid frame time spikes.
 */
enum class ExecutionStrategy {
    Adaptive,
    Deferred,
    Lazy
}