package me.blade.meshkt.renderer.font

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.objects.createBuffer
import me.blade.meshkt.renderer.objects.createFramebuffer
import me.blade.meshkt.renderer.objects.createShader
import me.blade.meshkt.renderer.objects.createTexture
import me.blade.meshkt.renderer.objects.framebuffer.properties.FramebufferAttachment
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import me.blade.meshkt.renderer.objects.texture.Texture
import me.blade.meshkt.renderer.objects.texture.properties.TextureInternalFormat
import me.blade.meshkt.renderer.objects.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.objects.texture.properties.TextureMinFilter
import me.blade.meshkt.renderer.objects.texture.properties.TexturePixelFormat
import me.blade.meshkt.renderer.objects.texture.properties.TexturePixelType
import me.blade.meshkt.renderer.util.resourceText
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11C.GL_VIEWPORT
import org.lwjgl.opengl.GL11C.glGetIntegerv
import org.lwjgl.opengl.GL11C.glViewport
import java.awt.image.BufferedImage

const val SDF_DOWNSCALE = 8
const val SDF_SCAN = 32

fun sdf(image: BufferedImage): Texture {
    val srcWidth = image.width
    val srcHeight = image.height
    val dstWidth = srcWidth / SDF_DOWNSCALE
    val dstHeight = srcHeight / SDF_DOWNSCALE

    val inputTexture = createTexture {
        storage {
            val textureData = createBuffer {
                repeat(srcHeight) { y ->
                    repeat(srcWidth) { x ->
                        byte(image.raster.getSample(x, y, 0).toByte())
                    }
                }
            }

            width = srcWidth
            height = srcHeight

            internalFormat = TextureInternalFormat.R8
            uploadPixelFormat = TexturePixelFormat.Red
            uploadPixelType = TexturePixelType.UnsignedByte

            allocate()
            upload(textureData.pointer)
            textureData.free()
        }
    }

    val outputTexture = createTexture {
        filtering {
            minFilter = TextureMinFilter.Linear
            magFilter = TextureMagFilter.Linear
        }

        storage {
            width = dstWidth
            height = dstHeight

            internalFormat = TextureInternalFormat.RGBA16F
            allocate()
        }
    }

    val framebuffer = createFramebuffer {
        attachments[FramebufferAttachment.Color0] = outputTexture
        drawTargets = arrayOf(FramebufferAttachment.Color0)
        validate()
    }

    val shader = createShader {
        compileSource(ShaderType.Vertex) {
            resourceText("/mesh/shaders/sdfgen.vsh")
        }

        compileSource(ShaderType.Fragment) {
            resourceText("/mesh/shaders/sdfgen.fsh")
        }

        link()

        storage.allocate("TextureBuffer") {
            long(inputTexture.bindlessHandle)
            upload()
        }
    }

    Mesh.boundShader = shader

    shader.uniforms {
        mat4("u_MATRIX", Matrix4f().ortho(0f, dstWidth.toFloat(), 0f, dstHeight.toFloat(), -1f, 1f))
        vec2("u_SRC_SIZE", srcWidth.toFloat(), srcHeight.toFloat())
        vec2("u_DST_SIZE", dstWidth.toFloat(), dstHeight.toFloat())
        int("u_SDF_DOWNSCALE", SDF_DOWNSCALE)
        int("u_SDF_SCAN", SDF_SCAN)
    }

    val prevFBO = Mesh.writeFramebuffer
    val prevViewport = IntArray(4).also {
        glGetIntegerv(GL_VIEWPORT, it)
    }

    glViewport(0, 0, dstWidth, dstHeight)
    Mesh.writeFramebuffer = framebuffer
    Mesh.render(shader, 1)
    Mesh.writeFramebuffer = prevFBO
    glViewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3])

    inputTexture.free()
    framebuffer.free()
    shader.free()

    return outputTexture
}