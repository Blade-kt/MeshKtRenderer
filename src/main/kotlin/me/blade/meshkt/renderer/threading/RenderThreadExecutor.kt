package me.blade.meshkt.renderer.threading

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import me.blade.meshkt.renderer.util.IMeshResource
import org.lwjgl.glfw.GLFW
import java.util.logging.Logger

class RenderThreadExecutor(
    val pullingStrategy: PullingStrategy
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

    val pendingActionCount: Int
        get() = synchronized(synchronizationLock) {
            pendingActions.size
        }

    val chainedActionCount: Int
        get() = synchronized(synchronizationLock) {
            chainedActions.size
        }

    val totalPendingActionCount: Int
        get() = synchronized(synchronizationLock) {
            pendingActions.size + chainedActions.size
        }

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

    override fun free() = synchronized(synchronizationLock) {
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