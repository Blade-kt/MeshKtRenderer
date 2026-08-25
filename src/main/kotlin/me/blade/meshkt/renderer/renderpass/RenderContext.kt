package me.blade.meshkt.renderer.renderpass

import me.blade.meshkt.renderer.objects.framebuffer.Framebuffer
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.util.ObservableEnumMap.Companion.observableEnumMap
import org.lwjgl.opengl.GL30C.GL_DRAW_FRAMEBUFFER
import org.lwjgl.opengl.GL30C.GL_READ_FRAMEBUFFER
import org.lwjgl.opengl.GL30C.glBindFramebuffer
import org.lwjgl.opengl.GL45C.glBindTextureUnit

class RenderContext {
    val boundTexture = observableEnumMap<TextureSlot, Texture?> { slot, texture ->
        glBindTextureUnit(slot.unitIndex, texture?.handle?.id ?: 0)
    }

    var readFramebuffer: Framebuffer? = null; set(value) {
        field = value
        glBindFramebuffer(GL_READ_FRAMEBUFFER, value?.handle?.id ?: 0)
    }

    var writeFramebuffer: Framebuffer? = null; set(value) {
        field = value
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, value?.handle?.id ?: 0)
    }
}