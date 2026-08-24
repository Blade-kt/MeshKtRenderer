package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.threading.GLThreadDispatcher
import java.util.logging.Logger

class MeshEngine(
    val logger: Logger,
    val dispatcher: GLThreadDispatcher
) : IMeshResource {
    val state = StateManager(this)
    val resources = ResourceFactory()

    override fun free() {
        this@MeshEngine.dispatcher.free()
        resources.free()
    }

    companion object {
        fun create(
            logger: Logger = Logger.getLogger("MeshKt"),
            dispatcher: GLThreadDispatcher = GLThreadDispatcher(logger = logger),
            block: MeshEngine.() -> Unit = {}
        ): MeshEngine {
            val engine = MeshEngine(logger, dispatcher)
            block(engine)
            return engine
        }
    }
}