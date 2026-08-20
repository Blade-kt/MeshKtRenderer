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
import kotlin.coroutines.CoroutineContext

class GLThreadDispatcher {
    private val pendingActions = mutableListOf<Runnable>()
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

    private val deferredScope = CoroutineScope(deferredDispatcher + SupervisorJob())
    private val adaptiveScope = CoroutineScope(adaptiveDispatcher + SupervisorJob())

    fun launch(forceDefer: Boolean = false, block: suspend () -> Unit): Job {
        val scope = when (forceDefer) {
            true -> deferredScope
            else -> adaptiveScope
        }

        return scope.launch {
            block()
        }
    }

    fun <R> async(forceDefer: Boolean = false, block: suspend () -> R): Deferred<R> {
        val scope = when (forceDefer) {
            true -> deferredScope
            else -> adaptiveScope
        }

        return scope.async {
            block()
        }
    }

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

    fun close() = synchronized(synchronizationLock) {
        pullPendingActionsUnsafe()
        renderThread = null
    }

    // marked as unsafe because the function is not synchronized itself
    private fun pullPendingActionsUnsafe() {
        pendingActions.forEach {
            it.run()
        }

        pendingActions.clear()
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