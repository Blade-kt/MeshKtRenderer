package me.blade.meshkt.renderer.objects.framebuffer

import me.blade.meshkt.renderer.objects.ObjectHandle
import me.blade.meshkt.renderer.objects.framebuffer.properties.FramebufferAttachment
import me.blade.meshkt.renderer.objects.framebuffer.properties.FramebufferStatus
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.util.ObservableMap.Companion.observableMap
import me.blade.meshkt.renderer.util.Quad
import org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER
import org.lwjgl.opengl.ARBFramebufferObject.glDeleteFramebuffers
import org.lwjgl.opengl.GL45C.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.properties.Delegates

open class Framebuffer(externalId: Int?) : ObjectHandle(
    externalId ?: glCreateFramebuffers(),
    externalId != null
) {
    private val _clearColor = FloatArray(4) { 0f }
    var clearColor = Quad(0f, 0f, 0f, 0f); set(value) {
        field = value
        _clearColor[0] = value.first
        _clearColor[1] = value.second
        _clearColor[2] = value.third
        _clearColor[3] = value.fourth
    }

    private val _clearDepth = FloatArray(1) { 1f }
    var clearDepth = 1.0; set(value) {
        field = value
        _clearDepth[0] = value.toFloat()
    }

    private val _clearStencil = IntArray(1) { 0 }
    var clearStencil = 0; set(value) {
        field = value
        _clearStencil[0] = value
    }

    fun invalidateAttachments(vararg attachments: FramebufferAttachment) {
        glInvalidateNamedFramebufferData(id, attachments.map { it.gl }.toIntArray())
    }

    fun clearAttachments(vararg attachments: FramebufferAttachment) {
        attachments.forEach(::clearAttachment)
    }

    private fun clearAttachment(attachment: FramebufferAttachment) {
        when (attachment) {
            in FramebufferAttachment.Color0..FramebufferAttachment.Color31 -> {
                val drawBuffer = attachment.ordinal - FramebufferAttachment.Color0.ordinal
                glClearNamedFramebufferfv(id, GL_COLOR, drawBuffer, _clearColor)
            }
            FramebufferAttachment.Depth -> {
                glClearNamedFramebufferfv(id, GL_DEPTH, 0, _clearDepth)
            }
            FramebufferAttachment.Stencil -> {
                glClearNamedFramebufferiv(id, GL_STENCIL, 0, _clearStencil)
            }
            FramebufferAttachment.DepthStencil -> {
                glClearNamedFramebufferfi(id, GL_DEPTH, 0, clearDepth.toFloat(), clearStencil)
            }
            else -> error("Unsupported attachment: $attachment")
        }
    }

    val attachments = observableMap<FramebufferAttachment, Texture?> { attachment, texture ->
        glNamedFramebufferTexture(id, attachment.gl, texture?.id ?: 0, texture?.attachmentLevel ?: 0)
    }

    var drawTargets by Delegates.observable(emptyArray<FramebufferAttachment>()) { _, _, value ->
        glNamedFramebufferDrawBuffers(id, value.map { it.gl }.toIntArray())
    }

    val status: FramebufferStatus get() {
        val glStatus = glCheckNamedFramebufferStatus(id, GL_FRAMEBUFFER)
        return FramebufferStatus.glMap[glStatus] ?: FramebufferStatus.Undefined
    }

    fun blitTo(
        destination: Framebuffer?,

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
            id,
            destination?.id ?: 0,
            srcX, srcY, srcX + srcWidth, srcY + srcHeight,
            dstX, dstY, dstX + dstWidth, dstY + dstHeight,
            mask, filter.gl
        )
    }

    fun blitFrom(
        source: Framebuffer?,

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
            source?.id ?: 0,
            id,
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

            check(attachment.isValid) {
                "Cannot attach texture to framebuffer: texture handle is invalid or has been freed."
            }
        }

        check(status == FramebufferStatus.Complete) {
            "Framebuffer is not complete: $status"
        }
    }

    fun freeAttachments() {
        setOf(*attachments.entries.toTypedArray()).forEach { (attachment, texture) ->
            attachments[attachment] = null
            texture?.free()
        }
    }

    override fun delete() {
        glDeleteFramebuffers(id)
    }
}