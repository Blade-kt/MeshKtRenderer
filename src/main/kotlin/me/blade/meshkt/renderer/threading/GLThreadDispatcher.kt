package me.blade.meshkt.renderer.threading

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import me.blade.meshkt.renderer.resource.MeshSyncContext
import org.lwjgl.glfw.GLFW
import java.util.List.copyOf
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

/**
 * A thread dispatcher for GLFW rendering operations that provides coroutine support
 * with configurable execution strategies.
 *
 * This dispatcher manages a dedicated render thread and provides two modes of operation:
 * - **Adaptive**: Executes immediately if on the render thread, otherwise defers to the next frame
 * - **Deferred**: Always defers execution to the next frame
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
 * glDispatcher.launch(forceDefer = true) {
 *     // This will always run next frame
 *     cleanupResources()
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
    private val postPendingActions = mutableListOf<Runnable>()
    private var renderThread: Thread? = null
    private val synchronizationLock = Any()

    private val deferredDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable): Unit = synchronized(synchronizationLock) {
            pendingActions.add(block)
        }
    }

    private val adaptiveDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable): Unit = synchronized(synchronizationLock) {
            if (isOnRenderThread()) {
                block.run()
                return@synchronized
            }

            pendingActions.add(block)
        }
    }

    private val postExecutionDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable): Unit = synchronized(synchronizationLock) {
            postPendingActions.add(block)
        }
    }

    private val deferredScope = CoroutineScope(deferredDispatcher + SupervisorJob())
    private val adaptiveScope = CoroutineScope(adaptiveDispatcher + SupervisorJob())
    private val postExecutionScope = CoroutineScope(postExecutionDispatcher + SupervisorJob())

    /**
     * Launches a new coroutine that will execute on the render thread.
     *
     * @param forceDefer If `true`, the coroutine will always execute in the next frame.
     *   If `false` (default), the coroutine executes immediately if called from the render thread,
     *   otherwise it defers to the next frame.
     * @param block The suspending block to execute on the render thread.
     * @return A [Job] representing the launched coroutine.
     *
     * ## Example:
     * ```kotlin
     * // Will execute immediately if on render thread
     * glDispatcher.launch {
     *     updateMesh()
     * }
     *
     * // Will always execute next frame
     * glDispatcher.launch(forceDefer = true) {
     *     cleanupResources()
     * }
     * ```
     */
    fun launch(forceDefer: Boolean = false, block: suspend () -> Unit): Job {
        val scope = when (forceDefer) {
            true -> deferredScope
            else -> adaptiveScope
        }

        return scope.launch {
            block()
        }
    }

    /**
     * Launches a new coroutine that will execute on the render thread and returns a deferred result.
     *
     * @param forceDefer If `true`, the coroutine will always execute in the next frame.
     *   If `false` (default), the coroutine executes immediately if called from the render thread,
     *   otherwise it defers to the next frame.
     * @param block The suspending block to execute on the render thread.
     * @return A [Deferred] representing the asynchronous computation.
     *
     * ## Example:
     * ```kotlin
     * val deferred = glDispatcher.async {
     *     loadTexture("player.png")
     * }
     *
     * // Await the result (will suspend)
     * val texture = deferred.await()
     * ```
     */
    fun <R> async(forceDefer: Boolean = false, block: suspend () -> R): Deferred<R> {
        val scope = when (forceDefer) {
            true -> deferredScope
            else -> adaptiveScope
        }

        return scope.async {
            block()
        }
    }

    /**
     * Suspends the current coroutine until all pending actions have been processed.
     *
     * This is useful when you need to ensure that all queued rendering operations
     * have completed before proceeding.
     *
     * ## Example:
     * ```kotlin
     * // From a background thread
     * glDispatcher.launch {
     *     loadTextures()
     * }
     *
     * // Wait for all texture loading to complete
     * glDispatcher.awaitAllPendingActions()
     *
     * // Now safe to proceed
     * println("All textures loaded!")
     * ```
     *
     * ## Warning:
     * Calling this from the render thread with runBlocking {} will cause a deadlock!
     * Only call from non-render threads.
     */
    suspend fun awaitAllPendingActions() {
        suspendCancellableCoroutine { continuation ->
            postExecutionScope.launch {
                continuation.resume(Unit)
            }
        }
    }

    /**
     * Processes all pending actions on the current thread.
     *
     * This method must be called from the GLFW render thread. It will:
     * 1. Verify or set up the render thread context
     * 2. Process all queued actions according to the configured [pullingStrategy]
     * 3. Clear the action queue
     *
     * **Important:** This method is annotated with [MeshSyncContext], indicating it should
     * only be called from the render thread context.
     *
     * ## Example:
     * ```kotlin
     * fun renderLoop() {
     *     glDispatcher.execute() // Process all queued actions
     *     glClear(GL_COLOR_BUFFER_BIT)
     *     // ... render scene
     * }
     * ```
     *
     * @throws IllegalStateException If called from a thread without a GLFW context
     *   or from a different thread after the render thread has been established.
     */
    @MeshSyncContext
    @Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
    fun execute() = synchronized(synchronizationLock) {
        val currentThread = Thread.currentThread()

        renderThread?.let {
            check(it == currentThread) {
                "Rendering context cannot be dispatched from different threads. Expected: ${it.name}, Actual: ${currentThread.name}"
            }
        } ?: run {
            check(isGLFWAvailable()) {
                "GLFW is not set up in given thread: ${currentThread.name}"
            }

            renderThread = currentThread
        }

        pullPendingActionsUnsafe()
    }

    /**
     * Closes the dispatcher and cleans up resources.
     *
     * This method:
     * 1. Processes any remaining pending actions
     * 2. Clears the render thread reference
     *
     * **Important:** This method is annotated with [MeshSyncContext], indicating it should
     * only be called from the render thread context.
     *
     * ## Example:
     * ```kotlin
     * fun shutdown() {
     *     glDispatcher.close()
     *     glfwDestroyWindow(window)
     *     glfwTerminate()
     * }
     * ```
     */
    @MeshSyncContext
    @Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")
    fun close() = synchronized(synchronizationLock) {
        pullPendingActionsUnsafe()
        renderThread = null
    }

    // marked as unsafe because the function is not synchronized itself
    private fun pullPendingActionsUnsafe() {
        when (pullingStrategy) {
            ActionPullingStrategy.Allocative -> {
                val actionsCopy = copyOf(pendingActions)
                pendingActions.clear()
                actionsCopy.forEach {
                    it.run()
                }

                val postActionsCopy = copyOf(postPendingActions)
                postPendingActions.clear()
                postActionsCopy.forEach {
                    it.run()
                }
            }
            ActionPullingStrategy.Direct -> {
                pendingActions.forEach {
                    it.run()
                }
                pendingActions.clear()

                postPendingActions.forEach {
                    it.run()
                }
                postPendingActions.clear()
            }
        }
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