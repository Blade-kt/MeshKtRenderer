package me.blade.meshkt.renderer.objects.framebuffer.properties

import org.lwjgl.opengl.GL30C.*

enum class FramebufferStatus(val gl: Int) {
    Complete(GL_FRAMEBUFFER_COMPLETE),
    IncompleteAttachment(GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT),
    MissingAttachment(GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT),
    IncompleteDrawBuffer(GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER),
    IncompleteReadBuffer(GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER),
    Unsupported(GL_FRAMEBUFFER_UNSUPPORTED),
    IncompleteMultisample(GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE),
    Undefined(GL_FRAMEBUFFER_UNDEFINED);

    companion object {
        val glMap = entries.associateBy(FramebufferStatus::gl)
    }
}