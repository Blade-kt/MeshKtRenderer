@file:Suppress("DSL_MARKER_APPLIED_TO_WRONG_TARGET")

package me.blade.meshkt.renderer
import me.blade.meshkt.renderer.objects.framebuffer.Framebuffer
import me.blade.meshkt.renderer.objects.framebuffer.FramebufferHandle
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.TextureHandle
import me.blade.meshkt.renderer.renderpass.RenderContextBridge
import me.blade.meshkt.renderer.threading.PullingStrategy
import me.blade.meshkt.renderer.threading.RenderThreadExecutor
import me.blade.meshkt.renderer.util.MeshDslObj3ct
import org.lwjgl.opengl.GL11C.GL_TEXTURE_2D
import org.lwjgl.opengl.GL45.glCreateTextures
import org.lwjgl.opengl.GL45C.glCreateFramebuffers
import java.util.logging.Logger

@MeshDslObj3ct
fun createRenderThreadExecutor(
    pullingStrategy: PullingStrategy = PullingStrategy.Allocative,
    logger: Logger
) = RenderThreadExecutor(pullingStrategy, logger)

@MeshDslObj3ct
fun createRenderContext(executor: RenderThreadExecutor) = RenderContextBridge(executor)

@MeshDslObj3ct
fun createTexture(
    handle: TextureHandle = createTextureHandle(),
    block: Texture.() -> Unit = {}
): Texture {
    val texture = Texture(handle)
    block(texture)
    return texture
}

@MeshDslObj3ct
fun createTextureHandle() = TextureHandle(glCreateTextures(GL_TEXTURE_2D), false)

@MeshDslObj3ct
fun externalTextureHandle(id: Int) = TextureHandle(id, true)

@MeshDslObj3ct
fun createFramebuffer(
    handle: FramebufferHandle = createFramebufferHandle(),
    validate: Boolean = true,
    block: Framebuffer.() -> Unit = {}
): Framebuffer {
    val framebuffer = Framebuffer(handle)
    block(framebuffer)
    if (validate) framebuffer.validate()
    return framebuffer
}

@MeshDslObj3ct
fun createFramebufferHandle() = FramebufferHandle(glCreateFramebuffers(), false)

@MeshDslObj3ct
fun externalFramebufferHandle(id: Int) = FramebufferHandle(id, true)