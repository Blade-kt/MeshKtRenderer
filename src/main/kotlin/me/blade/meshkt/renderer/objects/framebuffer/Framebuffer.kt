package me.blade.meshkt.renderer.objects.framebuffer

import me.blade.meshkt.renderer.objects.framebuffer.properties.FramebufferAttachment
import me.blade.meshkt.renderer.objects.framebuffer.properties.FramebufferStatus
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.resource.IMeshResource
import me.blade.meshkt.renderer.util.ObservableEnumMap.Companion.observableEnumMap
import org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER
import org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT
import org.lwjgl.opengl.GL45C.glBlitNamedFramebuffer
import org.lwjgl.opengl.GL45C.glCheckNamedFramebufferStatus
import org.lwjgl.opengl.GL45C.glNamedFramebufferDrawBuffers
import org.lwjgl.opengl.GL45C.glNamedFramebufferTexture
import kotlin.properties.Delegates

class Framebuffer(val handle: FramebufferHandle) : IMeshResource {
    val attachments = observableEnumMap<FramebufferAttachment, Texture?> { attachment, texture ->
        glNamedFramebufferTexture(handle.id, attachment.gl, texture?.handle?.id ?: 0, texture?.attachmentLevel ?: 0)
    }

    var drawTargets by Delegates.observable(emptyArray<FramebufferAttachment>()) { _, _, value ->
        glNamedFramebufferDrawBuffers(handle.id, value.map { it.gl }.toIntArray())
    }

    val status: FramebufferStatus get() {
        val glStatus = glCheckNamedFramebufferStatus(handle.id, GL_FRAMEBUFFER)
        return FramebufferStatus.glMap[glStatus] ?: FramebufferStatus.Undefined
    }

    fun blitTo(
        target: Framebuffer?,

        srcX: Int = 0,
        srcY: Int = 0,
        srcWidth: Int,
        srcHeight: Int,

        dstX: Int = srcX,
        dstY: Int = srcY,
        dstWidth: Int = srcWidth,
        dstHeight: Int = srcHeight,

        mask: Int = GL_COLOR_BUFFER_BIT,
        filter: TextureMagFilter = TextureMagFilter.Nearest,
    ) {
        glBlitNamedFramebuffer(
            handle.id,
            target?.handle?.id ?: 0,
            srcX, srcY, srcX + srcWidth, srcY + srcHeight,
            dstX, dstY, dstX + dstWidth, dstY + dstHeight,
            mask, filter.gl
        )
    }

    fun validate() {
        drawTargets.forEach { attachment ->
            val attachment = attachments[attachment]

            check(attachment != null) {
                "Framebuffer attachment '$attachment' is required by drawTargets but was not found in attachments map."
            }

            check(attachment.handle.isValid) {
                "Cannot attach texture to framebuffer: texture handle is invalid or has been freed."
            }
        }

        check(status == FramebufferStatus.Complete) {
            "Framebuffer is not complete: $status"
        }
    }

    override fun free() {
        handle.free()
    }
}