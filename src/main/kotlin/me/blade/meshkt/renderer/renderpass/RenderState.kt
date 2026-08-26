package me.blade.meshkt.renderer.renderpass

import me.blade.meshkt.renderer.objects.framebuffer.Framebuffer
import me.blade.meshkt.renderer.objects.ssbo.ShaderStorageBuffer
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureSlot
import me.blade.meshkt.renderer.util.ObservableEnumMap.Companion.observableEnumMap
import me.blade.meshkt.renderer.util.ObservableMap.Companion.observableMap
import org.lwjgl.opengl.GL30C.GL_DRAW_FRAMEBUFFER
import org.lwjgl.opengl.GL30C.GL_READ_FRAMEBUFFER
import org.lwjgl.opengl.GL30C.glBindBufferBase
import org.lwjgl.opengl.GL30C.glBindFramebuffer
import org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BLOCK
import org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER
import org.lwjgl.opengl.GL43C.glGetProgramResourceIndex
import org.lwjgl.opengl.GL43C.glShaderStorageBlockBinding
import org.lwjgl.opengl.GL45C.glBindTextureUnit

class RenderState(private val context: RenderContext) {
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



    fun validate() = context.validateState()
    fun release() = context.releaseState()
}