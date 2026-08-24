package me.blade.meshkt.renderer.engine

import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.util.ObservableEnumMap.Companion.observableEnumMap
import org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D
import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11C.glBindTexture
import org.lwjgl.opengl.GL11C.glGetInteger
import org.lwjgl.opengl.GL13C.GL_ACTIVE_TEXTURE
import org.lwjgl.opengl.GL13C.glActiveTexture

class StateManager(private val engine: MeshEngine) {
    private var prevTextureSlot = TextureSlot.Slot0.textureSlot
    private val prevBoundTextures = observableEnumMap<TextureSlot, Int> { _, _ -> }

    val boundTextures = observableEnumMap<TextureSlot, Texture?> { slot, texture ->
        glActiveTexture(slot.textureSlot)
        glBindTexture(GL_TEXTURE_2D, texture?.handle?.id ?: 0)
    }

    fun begin() {
        prevTextureSlot = glGetInteger(GL_ACTIVE_TEXTURE)

        TextureSlot.entries.forEach { slot ->
            glActiveTexture(slot.textureSlot)
            prevBoundTextures[slot] = glGetInteger(GL_TEXTURE_BINDING_2D)
            boundTextures[slot] = null
        }

        glActiveTexture(TextureSlot.Slot0.textureSlot)
    }

    fun end() {
        prevBoundTextures.entries.forEach { (slot, id) ->
            glActiveTexture(slot.textureSlot)
            glBindTexture(GL_TEXTURE_2D, id ?: 0)
        }

        glActiveTexture(prevTextureSlot)
    }
}