package me.blade.meshkt.renderer

import me.blade.meshkt.renderer.objects.framebuffer.createFramebuffer
import me.blade.meshkt.renderer.objects.framebuffer.properties.FramebufferAttachment
import me.blade.meshkt.renderer.objects.texture.createTexture
import me.blade.meshkt.renderer.objects.texture.properties.TextureInternalFormat
import me.blade.meshkt.renderer.threading.PullingStrategy
import java.util.logging.Logger

object MeshRenderer {
    private val logger: Logger = Logger.getLogger("MeshKt")

    val executor = createRenderThreadExecutor(PullingStrategy.Allocative, logger)
    val context = createRenderContext(executor)

    val framebuffer = createFramebuffer {
        attachments[FramebufferAttachment.Color0] = createTexture {
            storage {
                internalFormat = TextureInternalFormat.RGBA8
                width = 1024
                height = 1024
                allocate()
            }
        }

        attachments[FramebufferAttachment.Depth] = createTexture {
            storage {
                internalFormat = TextureInternalFormat.Depth32F
                width = 1024
                height = 1024
                allocate()
            }
        }

        drawTargets = arrayOf(FramebufferAttachment.Color0)
    }

    fun frame() {
        context.use {

        }
    }
}