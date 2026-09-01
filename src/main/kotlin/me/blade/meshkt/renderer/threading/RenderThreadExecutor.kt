package me.blade.meshkt.renderer.threading

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import me.blade.meshkt.renderer.util.IMeshResource
import org.lwjgl.glfw.GLFW
import java.util.logging.Logger

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
 * val glDispatcher = RenderThreadDispatcher()
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
 *   Defaults to [PullingStrategy.Allocative] for safety.
 *
 * @see PullingStrategy
 */

class RenderThreadExecutor(
    val pullingStrategy: PullingStrategy,
    val logger: Logger,
) : IMeshResource {
    private val pendingActions = mutableListOf<Runnable>()
    private val chainedActions = mutableListOf<Runnable>()
    private val actionCache = mutableListOf<Runnable>()

    private var renderThread: Thread? = null
    private val synchronizationLock = Any()

    private val adaptiveDispatcher: Dispatcher = { block ->
        synchronized(synchronizationLock) {
            if (isOnRenderThread()) {
                block.invoke()
            }

            pendingActions.add(block)
        }
    }

    private val deferredDispatcher: Dispatcher = { block ->
        synchronized(synchronizationLock) {
            pendingActions.add(block)
        }
    }

    private val chainedDispatcher: Dispatcher = { block ->
        synchronized(synchronizationLock) {
            chainedActions.add(block)
        }
    }

    private fun ExecutionStrategy.getExecutionDispatcher() = when (this) {
        ExecutionStrategy.Adaptive -> adaptiveDispatcher
        ExecutionStrategy.Deferred -> deferredDispatcher
        ExecutionStrategy.Lazy -> chainedDispatcher
    }

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
     * @param block The block to execute on the render thread.
     * @return A [Job] representing the launched coroutine.
     */
    fun launch(strategy: ExecutionStrategy = ExecutionStrategy.Adaptive, block: () -> Unit): Job {
        val dispatcher = strategy.getExecutionDispatcher()
        val job = Job()

        dispatcher.invoke {
            runCatching {
                block()
            }.onSuccess {
                job.complete()
            }.onFailure {
                job.completeExceptionally(it)
            }
        }

        return job
    }

    /**
     * Launches a new coroutine that returns a deferred result on the render thread.
     *
     * @param strategy The execution strategy. Defaults to [ExecutionStrategy.Adaptive]. (see: [ExecutionStrategy])
     * @param block The block to execute on the render thread.
     * @return A [Deferred] representing the asynchronous computation.
     */
    fun <R> deferred(strategy: ExecutionStrategy = ExecutionStrategy.Adaptive, block: () -> R): Deferred<R> {
        val dispatcher = strategy.getExecutionDispatcher()
        val deferred = CompletableDeferred<R>()

        dispatcher.invoke {
            runCatching {
                block()
            }.onSuccess {
                deferred.complete(it)
            }.onFailure {
                deferred.completeExceptionally(it)
            }
        }

        return deferred
    }

    /**
     * Processes all pending actions on the current thread.
     *
     * Must be called from the GLFW render thread AT THE BEGINNING OF THE FRAME. Execution order:
     * 1. All pending actions ([ExecutionStrategy.Adaptive] and [ExecutionStrategy.Deferred])
     * 2. One [ExecutionStrategy.Lazy] action (if any)
     *
     * @throws IllegalStateException If called without a GLFW context or from the wrong thread.
     */
    fun pollEvents() = synchronized(synchronizationLock) {
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
     * Frees the dispatcher and cleans up resources.
     *
     * Processes remaining actions and clears the render thread reference.
     * Must be called from the GLFW render thread.
     */
    override fun free() = synchronized(synchronizationLock) {
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
            PullingStrategy.Allocative -> {
                actionCache.clear()
                actionCache.addAll(pendingActions)
                actionCache.forEach { it.run() }
                pendingActions.clear()
            }
            PullingStrategy.Direct -> {
                pendingActions.forEach {
                    it.run()
                }
                pendingActions.clear()
            }
        }

        chainedActions.removeFirstOrNull()?.run()
    }

    fun isOnRenderThread() =
        renderThread?.let {
            it == Thread.currentThread()
        } ?: false

    companion object {
        private fun isGLFWAvailable() = runCatching {
            GLFW.glfwGetCurrentContext() != 0L
        }.getOrDefault(false)


    }
}

typealias Dispatcher = (block: () -> Unit) -> Unit