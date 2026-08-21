package me.blade.meshkt.renderer.threading

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import me.blade.meshkt.renderer.resource.MeshSyncContext
import org.lwjgl.glfw.GLFW
import java.util.List.copyOf
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext

/**
 * A thread dispatcher for GLFW rendering operations that provides coroutine support
 * with configurable execution strategies.
 *
 * This dispatcher manages a dedicated render thread and provides three modes of operation:
 * - **Adaptive**: Executes immediately if on the render thread, otherwise defers to the next frame
 * - **Deferred**: Always defers execution to the next frame
 * - **Lazy**: Defers actions to a separate action pool where only one action is processed per frame.
 *
 * ## Usage Example:
 * ```kotlin
 * val glDispatcher = GLThreadDispatcher()
 *
 * // In your render loop:
 * fun render() {
 *     glDispatcher.execute() // Process all queued actions
 *     // ... actual rendering
 * }
 *
 * // From any thread:
 * glDispatcher.launch {
 *     // This will run on the render thread
 *     updateTexture()
 * }
 *
 * // Force deferral to next frame:
 * glDispatcher.launch(strategy = ExecutionStrategy.Deferred) {
 *     // This will always run next frame
 *     cleanupResources()
 * }
 *
 * // Chained (1 action at frame) execution:
 * glDispatcher.launch(strategy = ExecutionStrategy.Lazy) {
 *     // This and only this will run on some frame
 *     updateRendererLazy()
 * }
 * ```
 *
 * @param pullingStrategy The strategy for processing pending actions.
 *   Defaults to [ActionPullingStrategy.Allocative] for safety.
 *
 * @see ActionPullingStrategy
 */
class GLThreadDispatcher(
    private val pullingStrategy: ActionPullingStrategy = ActionPullingStrategy.Allocative
) {
    private val pendingActions = mutableListOf<Runnable>()
    private val chainedActions = mutableListOf<Runnable>()
    private val actionCache = mutableListOf<Runnable>()

    private var renderThread: Thread? = null
    private val synchronizationLock = Any()

    private val logger = Logger.getLogger("MeshKt - GLThreadDispatcher")

    private val adaptiveDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable): Unit = synchronized(synchronizationLock) {
            if (isOnRenderThread()) {
                block.run()
                return@synchronized
            }

            pendingActions.add(block)
        }
    }

    private val deferredDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable): Unit = synchronized(synchronizationLock) {
            pendingActions.add(block)
        }
    }

    private val chainedDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable): Unit = synchronized(synchronizationLock) {
            chainedActions.add(block)
        }
    }

    private val adaptiveScope = CoroutineScope(adaptiveDispatcher + SupervisorJob())
    private val deferredScope = CoroutineScope(deferredDispatcher + SupervisorJob())
    private val chainedScope = CoroutineScope(chainedDispatcher + SupervisorJob())

    /**
     * Returns the number of pending actions in the main queue.
     * Thread-safe.
     */
    val pendingActionCount: Int
        get() = synchronized(synchronizationLock) {
            pendingActions.size
        }

    /**
     * Returns the number of pending actions in the chained (lazy) queue.
     * Thread-safe.
     */
    val chainedActionCount: Int
        get() = synchronized(synchronizationLock) {
            chainedActions.size
        }

    /**
     * Returns the total number of pending actions across all queues.
     * Thread-safe.
     */
    val totalPendingActionCount: Int
        get() = synchronized(synchronizationLock) {
            pendingActions.size + chainedActions.size
        }

    /**
     * Launches a new coroutine that will execute on the render thread.
     *
     * @param strategy The execution strategy. Defaults to [ExecutionStrategy.Adaptive]. (see: [ExecutionStrategy])
     * @param block The suspending block to execute on the render thread.
     * @return A [Job] representing the launched coroutine.
     */
    fun launch(strategy: ExecutionStrategy = ExecutionStrategy.Adaptive, block: suspend () -> Unit): Job {
        val scope = strategy.getExecutionScope()

        return scope.launch {
            block()
        }
    }

    /**
     * Launches a new coroutine that returns a deferred result on the render thread.
     *
     * @param strategy The execution strategy. Defaults to [ExecutionStrategy.Adaptive]. (see: [ExecutionStrategy])
     * @param block The suspending block to execute on the render thread.
     * @return A [Deferred] representing the asynchronous computation.
     */
    fun <R> async(strategy: ExecutionStrategy = ExecutionStrategy.Adaptive, block: suspend () -> R): Deferred<R> {
        val scope = strategy.getExecutionScope()

        return scope.async {
            block()
        }
    }

    /**
     * Processes all pending actions on the current thread.
     *
     * Must be called from the GLFW render thread. Execution order:
     * 1. All pending actions ([ExecutionStrategy.Adaptive] and [ExecutionStrategy.Deferred])
     * 2. One [ExecutionStrategy.Lazy] action (if any)
     *
     * @throws IllegalStateException If called without a GLFW context or from the wrong thread.
     */
    @MeshSyncContext
    @Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
    fun execute() = synchronized(synchronizationLock) {
        val currentThread = Thread.currentThread()

        when (val thread = renderThread) {
            null -> {
                check(isGLFWAvailable()) {
                    "GLFW is not set up in given thread: ${currentThread.name}"
                }

                renderThread = currentThread
            }
            else -> {
                check(thread == currentThread) {
                    "Rendering context cannot be dispatched from different threads. Expected: ${thread.name}, Actual: ${currentThread.name}"
                }
            }
        }

        pullPendingActionsUnsafe()
    }

    /**
     * Closes the dispatcher and cleans up resources.
     *
     * Processes remaining actions and clears the render thread reference.
     * Must be called from the GLFW render thread.
     */
    @MeshSyncContext
    @Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
    fun close() = synchronized(synchronizationLock) {
        if (renderThread == null) {
            logger.warning {
                "GLThreadDispatcher.close() was called when renderThread is null. " +
                        "Possibly the execute() method was never invoked or its not set up. " +
                        "That behaviour is not recommended."
            }
        }

        check(isOnRenderThread()) {
            "GLThreadDispatcher.close() must be called on GL rendering thread. Make sure the method is called before the GL context is destroyed."
        }

        pullPendingActionsUnsafe(pullAll = true)
        renderThread = null
    }

    // marked as unsafe because the function itself is not synchronized
    private fun pullPendingActionsUnsafe(pullAll: Boolean = false) {
        if (pullAll) {
            var iterations = 0
            val maxIterations = 1000 // Safety limit

            while (iterations < maxIterations) {
                actionCache.clear()
                actionCache.addAll(pendingActions)
                actionCache.forEach { it.run() }
                pendingActions.clear()

                actionCache.clear()
                actionCache.addAll(chainedActions)
                actionCache.forEach { it.run() }
                chainedActions.clear()

                iterations++
                if (pendingActions.isEmpty() && chainedActions.isEmpty()) break
            }

            if (iterations >= maxIterations) {
                logger.warning {
                    "GLThreadDispatcher.close() exceeded max iterations ($maxIterations). " +
                            "Possible infinite action loop. Dropping remaining actions: pending=${pendingActions.size}, chained=${chainedActions.size}"
                }

                pendingActions.clear()
                chainedActions.clear()
            }

            return
        }

        when (pullingStrategy) {
            ActionPullingStrategy.Allocative -> {
                actionCache.clear()
                actionCache.addAll(pendingActions)
                actionCache.forEach { it.run() }
                pendingActions.clear()
            }
            ActionPullingStrategy.Direct -> {
                pendingActions.forEach {
                    it.run()
                }
                pendingActions.clear()
            }
        }

        chainedActions.removeFirstOrNull()?.run()
    }

    private fun ExecutionStrategy.getExecutionScope() = when (this) {
        ExecutionStrategy.Adaptive -> adaptiveScope
        ExecutionStrategy.Deferred -> deferredScope
        ExecutionStrategy.Lazy -> chainedScope
    }

    private fun isOnRenderThread() =
        renderThread?.let {
            it == Thread.currentThread()
        } ?: false

    companion object {
        private fun isGLFWAvailable() = runCatching {
            GLFW.glfwGetCurrentContext() != 0L
        }.getOrDefault(false)
    }
}