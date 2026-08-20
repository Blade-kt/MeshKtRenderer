package me.blade.meshkt.renderer.threading

/**
 * Strategy for processing pending actions during [GLThreadDispatcher.execute].
 *
 * [Allocative] Creates a snapshot of pending actions before execution.
 *   - Safe for re-entrant calls (actions added during execution will run next frame)
 *   - Small GC overhead (allocates one list per frame)
 *   - Recommended for most use cases
 *
 * [Direct] Iterates over pending actions directly without copying.
 *   - Zero allocation during execution
 *   - NOT re-entrant safe: actions added during execution will run in the SAME frame
 *   - Use only if you're certain actions won't add more actions
 *   - Best for performance-critical code with predictable execution
 */
enum class ActionPullingStrategy {
    Allocative,
    Direct
}