package me.blade.meshkt

import me.blade.meshkt.renderer.Mesh
import me.blade.meshkt.renderer.font.buildGlyphMap
import me.blade.meshkt.renderer.font.sdf
import me.blade.meshkt.renderer.objects.createBuffer
import me.blade.meshkt.renderer.objects.createShader
import me.blade.meshkt.renderer.objects.createTexture
import me.blade.meshkt.renderer.objects.shader.properties.ShaderType
import me.blade.meshkt.renderer.objects.texture.properties.TextureInternalFormat
import me.blade.meshkt.renderer.objects.texture.properties.TextureMagFilter
import me.blade.meshkt.renderer.objects.texture.properties.TextureMinFilter
import me.blade.meshkt.renderer.objects.texture.properties.TexturePixelFormat
import me.blade.meshkt.renderer.objects.texture.properties.TexturePixelType
import me.blade.meshkt.renderer.util.packVec2
import me.blade.meshkt.renderer.util.packVec3
import org.joml.Matrix4f
import java.awt.Font

object MeshRenderer {
    val glyphMap = buildGlyphMap(
        Font("SansSerif", Font.PLAIN, 128)
    )

    val sdfTexture = createTexture {
        val image = sdf(glyphMap.image)
        val buffer = createBuffer {
            repeat(image.height) { y ->
                repeat(image.width) { x ->
                    byte(image.raster.getSample(x, y, 0).toByte())
                }
            }
        }

        filtering {
            minFilter = TextureMinFilter.Linear
            magFilter = TextureMagFilter.Linear
        }

        storage {
            width = image.width
            height = image.height

            internalFormat = TextureInternalFormat.R8
            uploadPixelFormat = TexturePixelFormat.Red
            uploadPixelType = TexturePixelType.UnsignedByte

            allocate()
            upload(buffer.pointer)
        }
        buffer.free()
    }

    val shader = createShader {
        fun loadShaderText(path: String): String {
            val stream = javaClass.getResourceAsStream(path)!!
            return stream.bufferedReader(Charsets.UTF_8).readText()
        }

        compileSource(ShaderType.Vertex) {
            loadShaderText("/mesh/shaders/renderer.vsh")
        }

        compileSource(ShaderType.Fragment) {
            loadShaderText("/mesh/shaders/renderer.fsh")
        }

        link()
        storage.allocateNames(1024,
            "TextureBuffer", "MatrixBuffer", "InstanceBuffer",
            "RectInstanceBuffer",
            "StringInstanceBuffer", "CharInstanceBuffer", "GlyphBuffer"
        )

        storage.write("GlyphBuffer") {
            reset()
            glyphMap.charData.values.forEach { glyphData ->
                vec2(glyphData.u0, glyphData.v0)
                vec2(glyphData.u1, glyphData.v1)
            }
            upload()
        }

    }

    var frameCount = 0
    private val time get() = System.currentTimeMillis()
    var lastPrintTime = time

    fun frame() {
        val textureHandles = Array<Long>(16) { 0 }
        frameCount++

        if (time - lastPrintTime > 1000) {
            println(frameCount)
            lastPrintTime = time
            frameCount = 0
        }

        shader.storage.write("MatrixBuffer") {
            reset()

            // proj
            mat4(Matrix4f().ortho(0f, MeshRendererExample.viewportWidth.toFloat(), MeshRendererExample.viewportHeight.toFloat(), 0f, -10000f, 10000f))
            // view
            mat4(Matrix4f().identity())
            // model
            mat4(Matrix4f().identity())

            upload()
        }

        shader.storage.write("InstanceBuffer") {
            reset()

            // 4 bits for buffer index, 28 for instance index
            int(packVec2(4, 28, 1, 0)) // B
            int(packVec2(4, 28, 1, 1)) // l
            int(packVec2(4, 28, 1, 2)) // a
            int(packVec2(4, 28, 1, 3)) // d
            int(packVec2(4, 28, 1, 4)) // e
            int(packVec2(4, 28, 1, 5)) // B
            int(packVec2(4, 28, 1, 6)) // l
            int(packVec2(4, 28, 1, 7)) // a

            upload()
        }

        val height = 200.0
        shader.storage.write("StringInstanceBuffer") {
            reset()

            int(packVec3(4, 20, 8, 0, 1, 2)) // matrices
            float(height)

            upload()
        }

        shader.storage.write("CharInstanceBuffer") {
            reset()

            var x = 0.0
            "Abpd_Efg.".forEach {
                vec2(10.0 + x, 200.0) // position
                int(0) // string index
                int(glyphMap.charIndexMap[it]!!)
                x += glyphMap.charData[it]!!.getCharWidth(height)
            }

            upload()
        }

        textureHandles[0] = sdfTexture.bindlessHandle
        shader.storage.write("TextureBuffer") {
            reset()
            textureHandles.forEach { ptr ->
                long(ptr)
            }
            upload()
        }

        Mesh.boundShader = shader
        shader.uniforms.float("u_NORMALIZED_FONT_BASELINE", glyphMap.normalizedBaseline)
        Mesh.render(shader, 8)
    }
}