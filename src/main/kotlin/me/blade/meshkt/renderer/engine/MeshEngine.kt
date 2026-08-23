package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.resource.MeshSyncContext
import me.blade.meshkt.renderer.resource.ResourceFactory
import me.blade.meshkt.renderer.threading.GLThreadDispatcher
import me.blade.meshkt.renderer.util.ObservableEnumMap.Companion.observableEnumMap
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D
import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11C.glGetInteger
import org.lwjgl.opengl.GL11C.glGetIntegerv
import org.lwjgl.opengl.GL13C.GL_ACTIVE_TEXTURE
import org.lwjgl.opengl.GL13C.GL_TEXTURE0
import org.lwjgl.opengl.GL13C.glActiveTexture
import org.lwjgl.opengl.GL45C.glBindTextureUnit
import org.lwjgl.opengl.GL45C.glCreateTextures
import sun.rmi.server.Dispatcher
import java.util.logging.Logger

class MeshEngine(
    val logger: Logger,
    val dispatcher: GLThreadDispatcher
) : IMeshResource {
    val resourceFactory = ResourceFactory(this)

    val boundTextures = observableEnumMap<TextureSlot, Texture?> { slot, texture ->
        glBindTextureUnit(slot.gl, texture?.handle?.id ?: 0)
    }

    fun syncBoundTextures() {
        TextureSlot.entries.forEach { slot ->
            glActiveTexture(slot.gl)
            glGetInteger(GL_TEXTURE_BINDING_2D)
        }
    }

    @MeshSyncContext
    override fun free() {
        this@MeshEngine.dispatcher.free()
        resourceFactory.free()
    }

    companion object {
        @MeshSyncContext
        fun create(
            logger: Logger = Logger.getLogger("MeshKt"),
            dispatcher: GLThreadDispatcher = GLThreadDispatcher(logger = logger),
            block: MeshEngine.() -> Unit = {}
        ): MeshEngine {
            fun getActiveTexture() {
            }

            val engine = MeshEngine(logger, dispatcher)
            block(engine)
            return engine
        }
    }
}